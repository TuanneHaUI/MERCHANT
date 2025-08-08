package com.teamphacode.MerchantManagement.service;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqUpdateMerchant;
import com.teamphacode.MerchantManagement.domain.dto.response.MerchantResponse;
import com.teamphacode.MerchantManagement.domain.dto.response.ResMerchantYearStatusDTO;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface MerchantService  {

    MerchantResponse handleCreateMerchant(MerchantCreateRequest request);

    Merchant handleUpdateMerchant(ReqUpdateMerchant reqUpdateMerchant);

    ResultPaginationDTO handleReportMerchantByStatus(StatusEnum statusEnum, Pageable pageable) throws IdInvalidException;

    List<ResMerchantYearStatusDTO> handleCountMerchantActiveByYear(int year);

    ResultPaginationDTO handleFindByMerchantIdAndAccountNoAndStatus(String merchantId, String accountNo, StatusEnum status, Pageable pageable);

}




