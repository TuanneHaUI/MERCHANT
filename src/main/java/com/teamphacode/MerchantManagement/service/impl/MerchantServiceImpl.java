package com.teamphacode.MerchantManagement.service.impl;

import com.teamphacode.MerchantManagement.config.MerchantIdConfig;
import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqUpdateMerchant;
import com.teamphacode.MerchantManagement.domain.dto.response.MerchantResponse;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.mapper.MerchantMapper;
import com.teamphacode.MerchantManagement.repository.MerchantRepository;
import com.teamphacode.MerchantManagement.service.MerchantService;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import com.teamphacode.MerchantManagement.util.errors.AppException;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
     private RedisTemplate<String, Object> redisTemplate;
     private static final String HASH_KEY = "active_merchants";


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
    public Merchant handleUpdateMerchant(ReqUpdateMerchant reqUpdateMerchant) {
        return null;
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
}