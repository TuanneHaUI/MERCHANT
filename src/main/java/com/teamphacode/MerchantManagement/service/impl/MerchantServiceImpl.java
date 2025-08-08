package com.teamphacode.MerchantManagement.service.impl;

import com.teamphacode.MerchantManagement.config.MerchantIdConfig;
import com.teamphacode.MerchantManagement.domain.MCC;
import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.MerchantHistory;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqUpdateMerchant;
import com.teamphacode.MerchantManagement.domain.dto.response.MerchantResponse;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.mapper.MerchantMapper;
import com.teamphacode.MerchantManagement.repository.MCCRepository;
import com.teamphacode.MerchantManagement.repository.MerchantHistoryRepository;
import com.teamphacode.MerchantManagement.repository.MerchantRepository;
import com.teamphacode.MerchantManagement.service.MerchantService;
import com.teamphacode.MerchantManagement.util.SecurityUtil;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import com.teamphacode.MerchantManagement.util.errors.AppException;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchantServiceImpl implements MerchantService {
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantIdConfig merchantIdConfig;
    @Autowired
    private MCCRepository mccRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private static final String HASH_KEY = "active_merchants";
    @Autowired
    private MerchantHistoryRepository merchantHistoryRepository;

    @Override
    public MerchantResponse handleCreateMerchant(MerchantCreateRequest request){
        Merchant merchant = merchantMapper.toMerchant(request);
        merchant.setMerchantId(merchantIdConfig.generateMerchantId());
        if (merchantRepository.existsByAccountNo(request.getAccountNo())) {
            throw new AppException("accountNo bị trùng", 400);
        }
        return merchantMapper.toMerchantResponse(merchantRepository.save(merchant));
    }

    @Override
    public boolean isChanged(Object oldVal, Object newVal) {
        if (newVal == null) return false;
        return !newVal.equals(oldVal);
    }


    @Override
    @Transactional
    public Merchant handleUpdateMerchant(ReqUpdateMerchant reqUpdateMerchant) throws IdInvalidException {
        Merchant merchant = merchantRepository.findByAccountNo(reqUpdateMerchant.getAccountNo())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy merchant với số tài khoản: " + reqUpdateMerchant.getAccountNo()));

        if (reqUpdateMerchant.getStatus() == StatusEnum.Close && reqUpdateMerchant.getCloseDate() == null) {
            throw new RuntimeException("Khi cập nhật trạng thái sang ĐÓNG, phải nhập thời điểm kết thúc hoạt động (closeDate).");
        }

        StringBuilder changeContent = new StringBuilder();

        if (isChanged(merchant.getFullName(), reqUpdateMerchant.getFullName())) {
            changeContent.append(String.format("fullName: '%s' → '%s'; ", merchant.getFullName(), reqUpdateMerchant.getFullName()));
            merchant.setFullName(reqUpdateMerchant.getFullName());
        }
        if (isChanged(merchant.getShortName(), reqUpdateMerchant.getShortName())) {
            changeContent.append(String.format("shortName: '%s' → '%s'; ", merchant.getShortName(), reqUpdateMerchant.getShortName()));
            merchant.setShortName(reqUpdateMerchant.getShortName());
        }
        if (isChanged(merchant.getMcc(), reqUpdateMerchant.getMcc())) {
            changeContent.append(String.format("mcc: '%s' → '%s'; ", merchant.getMcc(), reqUpdateMerchant.getMcc()));
            merchant.setMcc(reqUpdateMerchant.getMcc());
        }
        if (isChanged(merchant.getCity(), reqUpdateMerchant.getCity())) {
            changeContent.append(String.format("city: '%s' → '%s'; ", merchant.getCity(), reqUpdateMerchant.getCity()));
            merchant.setCity(reqUpdateMerchant.getCity());
        }
        if (isChanged(merchant.getLocation(), reqUpdateMerchant.getLocation())) {
            changeContent.append(String.format("location: '%s' → '%s'; ", merchant.getLocation(), reqUpdateMerchant.getLocation()));
            merchant.setLocation(reqUpdateMerchant.getLocation());
        }
        if (isChanged(merchant.getPhoneNo(), reqUpdateMerchant.getPhoneNo())) {
            changeContent.append(String.format("phoneNo: '%s' → '%s'; ", merchant.getPhoneNo(), reqUpdateMerchant.getPhoneNo()));
            merchant.setPhoneNo(reqUpdateMerchant.getPhoneNo());
        }
        if (isChanged(merchant.getEmail(), reqUpdateMerchant.getEmail())) {
            changeContent.append(String.format("email: '%s' → '%s'; ", merchant.getEmail(), reqUpdateMerchant.getEmail()));
            merchant.setEmail(reqUpdateMerchant.getEmail());
        }
        if (isChanged(merchant.getOpenDate(), reqUpdateMerchant.getOpenDate())) {
            changeContent.append(String.format("openDate: '%s' → '%s'; ", merchant.getOpenDate(), reqUpdateMerchant.getOpenDate()));
            merchant.setOpenDate(reqUpdateMerchant.getOpenDate());
        }
        if (isChanged(merchant.getCloseDate(), reqUpdateMerchant.getCloseDate())) {
            changeContent.append(String.format("closeDate: '%s' → '%s'; ", merchant.getCloseDate(), reqUpdateMerchant.getCloseDate()));
            merchant.setCloseDate(reqUpdateMerchant.getCloseDate());
        }
        if (isChanged(merchant.getStatus(), reqUpdateMerchant.getStatus())) {
            changeContent.append(String.format("status: '%s' → '%s'; ", merchant.getStatus(), reqUpdateMerchant.getStatus()));
            merchant.setStatus(reqUpdateMerchant.getStatus());
        }

        if (!changeContent.toString().isBlank()) {
            if(reqUpdateMerchant.getReason() == null){
                throw new IdInvalidException("Reason null");
            }
            MerchantHistory history = MerchantHistory.builder()
                    .merchantId(merchant.getMerchantId())
                    .accountNo(merchant.getAccountNo())
                    .changedAt(LocalDateTime.now())
                    .changedBy(SecurityUtil.getCurrentUserLogin().isPresent() == true
                            ? SecurityUtil.getCurrentUserLogin().get()
                            : "")
                    .changeContent(changeContent.toString())
                    .reason(reqUpdateMerchant.getReason())
                    .build();
            Boolean exists = redisTemplate.hasKey("MerchantHistoryServiceImpl");
            if(Boolean.TRUE.equals(exists)){
                redisTemplate.delete("MerchantHistoryServiceImpl");
            }
            merchantHistoryRepository.save(history);
        }

        return merchantRepository.save(merchant);
    }


    @Override
    public ResultPaginationDTO handleReportMerchantByStatus(StatusEnum statusEnum, Pageable pageable)
             throws IdInvalidException {

         String key = "report_by_status_" + statusEnum.name() + "_page_" + pageable.getPageNumber() +
                 "_size_" + pageable.getPageSize() + "_sort_" + pageable.getSort().toString();

         List<Merchant> cacheMerchants = (List<Merchant>) redisTemplate.opsForHash().get(HASH_KEY, key);
         Page<Merchant> merchantPage;

         if (cacheMerchants != null) {
             merchantPage = new PageImpl<>(cacheMerchants, pageable, cacheMerchants.size());
         } else {
             merchantPage = merchantRepository.findByStatus(statusEnum, pageable);
             if (merchantPage.isEmpty()) {
                 throw new IdInvalidException("không có dữ liệu của merchant với trạng thái " + statusEnum);
             }
             redisTemplate.opsForHash().put(HASH_KEY, key, merchantPage.getContent());
             redisTemplate.expire(HASH_KEY, Duration.ofHours(1));
         }

         // Tạo ResultPaginationDTO
         ResultPaginationDTO dto = new ResultPaginationDTO();
         ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
         meta.setPage(merchantPage.getNumber());
         meta.setPageSize(merchantPage.getSize());
         meta.setPages(merchantPage.getTotalPages());
         meta.setTotal(merchantPage.getTotalElements());

         dto.setMeta(meta);
         dto.setResult(merchantPage.getContent());

         return dto;
     }

    @Override
    public Merchant findMerchantByAccountNo(String accountNo) {
        return null;
    }
}