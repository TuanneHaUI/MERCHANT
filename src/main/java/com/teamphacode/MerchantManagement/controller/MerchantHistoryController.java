package com.teamphacode.MerchantManagement.controller;


import com.teamphacode.MerchantManagement.domain.MerchantHistory;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqMerchantHistory;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.service.impl.MerchantHistoryServiceImpl;
import com.teamphacode.MerchantManagement.service.impl.MerchantServiceImpl;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MerchantHistoryController {
    @Autowired
    private MerchantHistoryServiceImpl merchantHistoryService;

    @GetMapping("/merchant/history")
    public ResponseEntity<ResultPaginationDTO> getMerchantHistory(@Filter Specification<MerchantHistory> spec, Pageable pageable){

        return ResponseEntity.ok(this.merchantHistoryService.handleGetMerchantHistoryByMerchantIdOrAccountNo(spec, pageable));
    }

}
