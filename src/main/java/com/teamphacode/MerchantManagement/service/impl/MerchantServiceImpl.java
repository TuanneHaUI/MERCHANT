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
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
public class MerchantServiceImpl implements MerchantService {
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantIdConfig merchantIdConfig;
    @Autowired
    private Validator validator;
     @Autowired
     private RedisTemplate<String, Object> redisTemplate;
     private static final String HASH_KEY = "active_merchants";


    @Override
    public MerchantResponse handleCreateMerchant(MerchantCreateRequest request){
        Merchant merchant = merchantMapper.toMerchant(request);
        if (merchantRepository.existsByAccountNo(request.getAccountNo())) {
            throw new AppException("accountNo bị trùng", 400);
        }
        if (merchantRepository.existsById(request.getMerchantId())) {
            throw new AppException("merchantId bị trùng", 400);
        }
        return merchantMapper.toMerchantResponse(merchantRepository.save(merchant));
    }

    @Override
    public List<Merchant> getAll() {
        return merchantRepository.findAll();
    }

    @Override
    @Transactional
    public void handleCreateMultipleMerchants(List<MerchantCreateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        // --- BƯỚC 1: KIỂM TRA TRÙNG LẶP TRONG FILE ---
        Set<String> accountNosInFile = new HashSet<>();
        Set<String> merchantIdsInFile = new HashSet<>();
        Map<String, List<Integer>> duplicateAccountNosInFile = new HashMap<>();
        Map<String, List<Integer>> duplicateMerchantIdsInFile = new HashMap<>();

        for (int i = 0; i < requests.size(); i++) {
            String accountNo = requests.get(i).getAccountNo();
            String merchantId = requests.get(i).getMerchantId();
            int rowNum = i + 2;

            if (accountNo != null && !accountNo.isEmpty() && !accountNosInFile.add(accountNo)) {
                duplicateAccountNosInFile.computeIfAbsent(accountNo, k -> new ArrayList<>()).add(rowNum);
            }
            if (merchantId != null && !merchantId.isEmpty() && !merchantIdsInFile.add(merchantId)) {
                duplicateMerchantIdsInFile.computeIfAbsent(merchantId, k -> new ArrayList<>()).add(rowNum);
            }
        }

        StringBuilder fileErrors = new StringBuilder();
        duplicateAccountNosInFile.forEach((acc, rows) -> fileErrors.append("Số tài khoản '").append(acc).append("' bị trùng ở các dòng: ").append(rows).append(". "));
        duplicateMerchantIdsInFile.forEach((mid, rows) -> fileErrors.append("Mã định danh '").append(mid).append("' bị trùng ở các dòng: ").append(rows).append(". "));

        if (fileErrors.length() > 0) {
            throw new AppException("Phát hiện dữ liệu trùng lặp trong file: " + fileErrors.toString(), 400);
        }

        // --- BƯỚC 2: KIỂM TRA TRÙNG LẶP HÀNG LOẠT VỚI CSDL ---
        Set<String> existingAccountNos = merchantRepository.findExistingAccountNos(accountNosInFile);
        Set<String> existingMerchantIds = merchantRepository.findExistingMerchantIds(merchantIdsInFile);

        // --- BƯỚC 3: VALIDATE CHI TIẾT VÀ TỔNG HỢP LỖI ---
        List<Merchant> merchantsToSave = new ArrayList<>();
        StringBuilder allErrors = new StringBuilder();

        for (int i = 0; i < requests.size(); i++) {
            MerchantCreateRequest request = requests.get(i);
            int rowNum = i + 2;
            StringBuilder lineErrors = new StringBuilder(); // Dùng để thu thập lỗi của riêng dòng này

            // a. Kiểm tra lỗi validation bean
            Set<ConstraintViolation<MerchantCreateRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                violations.forEach(v -> lineErrors.append(v.getPropertyPath()).append(": ").append(v.getMessage()).append("; "));
            }

            // b. Kiểm tra lỗi trùng lặp với CSDL
            if (request.getAccountNo() != null && existingAccountNos.contains(request.getAccountNo())) {
                lineErrors.append("Số tài khoản '").append(request.getAccountNo()).append("' đã tồn tại; ");
            }
            if (request.getMerchantId() != null && existingMerchantIds.contains(request.getMerchantId())) {
                lineErrors.append("Mã định danh '").append(request.getMerchantId()).append("' đã tồn tại; ");
            }

            // c. Tổng hợp kết quả của dòng này
            if (lineErrors.length() > 0) {
                // Nếu có lỗi, thêm vào danh sách lỗi chung
                allErrors.append("Dòng ").append(rowNum).append(": ").append(lineErrors);
            } else {
                // Nếu không có lỗi, thêm vào danh sách để lưu
                merchantsToSave.add(merchantMapper.toMerchant(request));
            }
        }

        // --- BƯỚC 4: RA QUYẾT ĐỊNH CUỐI CÙNG ---
        if (allErrors.length() > 0) {
            // Nếu có BẤT KỲ lỗi nào được ghi nhận, ném ra exception với tất cả các lỗi
            throw new AppException("Dữ liệu trong file không hợp lệ: " + allErrors.toString(), 400);
        }

        // Chỉ lưu nếu danh sách không rỗng và không có lỗi nào xảy ra
        if (!merchantsToSave.isEmpty()) {
            merchantRepository.saveAll(merchantsToSave);
        }
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