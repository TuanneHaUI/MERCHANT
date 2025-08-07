package com.teamphacode.MerchantManagement.service.impl;

import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.response.MerchantResponse;

public interface MerchantServiceImpl {
    MerchantResponse handleCreateMerchant(MerchantCreateRequest request);
}