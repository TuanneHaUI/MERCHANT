package com.teamphacode.MerchantManagement.service;

import com.teamphacode.MerchantManagement.domain.MerchantHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface MerchantHistoryService {

     List<MerchantHistory> handleGetMerchantHistoryByMerchantIdOrAccountNo(Specification<MerchantHistory> spec, Pageable pageable);

}
