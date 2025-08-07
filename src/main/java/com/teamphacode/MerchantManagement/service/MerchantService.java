package com.teamphacode.MerchantManagement.service;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqUpdateMerchant;
import com.teamphacode.MerchantManagement.domain.dto.response.MerchantResponse;



public interface MerchantService  {

    MerchantResponse handleCreateMerchant(MerchantCreateRequest request);

    Merchant handleUpdateMerchant(ReqUpdateMerchant reqUpdateMerchant);
}
