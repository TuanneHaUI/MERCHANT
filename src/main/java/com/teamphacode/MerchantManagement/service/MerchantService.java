package com.teamphacode.MerchantManagement.service;

import com.teamphacode.MerchantManagement.config.MerchantIdConfig;
import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.response.MerchantResponse;
import com.teamphacode.MerchantManagement.mapper.MerchantMapper;
import com.teamphacode.MerchantManagement.repository.MerchantRepository;
import com.teamphacode.MerchantManagement.service.impl.MerchantServiceImpl;
import com.teamphacode.MerchantManagement.util.errors.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MerchantService implements MerchantServiceImpl {
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantIdConfig merchantIdConfig;
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

}
