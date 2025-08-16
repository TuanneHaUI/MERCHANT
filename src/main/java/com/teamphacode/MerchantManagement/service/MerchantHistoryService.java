package com.teamphacode.MerchantManagement.service;

import com.teamphacode.MerchantManagement.domain.MerchantHistory;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface MerchantHistoryService {

     ResultPaginationDTO handleGetMerchantHistoryByMerchantIdOrAccountNo(Specification<MerchantHistory> spec, Pageable pageable);

}
