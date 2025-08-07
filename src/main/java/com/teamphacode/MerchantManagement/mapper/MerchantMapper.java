package com.teamphacode.MerchantManagement.mapper;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.response.MerchantResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MerchantMapper {
    Merchant toMerchant(MerchantCreateRequest request);
    MerchantResponse toMerchantResponse(Merchant merchant);
}
