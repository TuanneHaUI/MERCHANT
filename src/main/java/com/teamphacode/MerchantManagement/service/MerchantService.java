package com.teamphacode.MerchantManagement.service;

import com.teamphacode.MerchantManagement.config.MerchantIdConfig;
import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.response.MerchantResponse;
import com.teamphacode.MerchantManagement.mapper.MerchantMapper;
import com.teamphacode.MerchantManagement.repository.MerchantRepository;
import com.teamphacode.MerchantManagement.service.impl.MerchantServiceImpl;
import com.teamphacode.MerchantManagement.util.errors.AppException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class MerchantService implements MerchantServiceImpl {
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantIdConfig merchantIdConfig;
    @Autowired
    private Validator validator;
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
    public List<Merchant> getAll() {
        return merchantRepository.findAll();
    }

    @Override
    @Transactional // Đảm bảo tất cả được lưu hoặc không lưu gì cả
    public void handleCreateMultipleMerchants(List<MerchantCreateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        List<Merchant> merchantsToSave = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            MerchantCreateRequest request = requests.get(i);

            // 1. Validate DTO
            Set<ConstraintViolation<MerchantCreateRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Lỗi dữ liệu ở dòng ").append(i + 2).append(" trong file Excel: ");
                for (ConstraintViolation<MerchantCreateRequest> violation : violations) {
                    sb.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("; ");
                }
                throw new IllegalArgumentException(sb.toString());
            }

            Merchant merchant = merchantMapper.toMerchant(request);

            merchant.setMerchantId(merchantIdConfig.generateMerchantId());

            merchantsToSave.add(merchant);
        }

        merchantRepository.saveAll(merchantsToSave);
    }

}
