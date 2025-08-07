package com.teamphacode.MerchantManagement.service.impl;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.response.MerchantResponse;

import java.util.List;

public interface MerchantServiceImpl {
    MerchantResponse handleCreateMerchant(MerchantCreateRequest request);
    List<Merchant> getAll();
    void handleCreateMultipleMerchants(List<MerchantCreateRequest> requests);
}