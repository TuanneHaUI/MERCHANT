package com.teamphacode.MerchantManagement.controller;

import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.response.MerchantResponse;
import com.teamphacode.MerchantManagement.domain.dto.response.RestResponse;
import com.teamphacode.MerchantManagement.service.MerchantService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@Validated
public class MerchantController {
    @Autowired
    private MerchantService merchantService;
    @PostMapping("/merchant/create")
    RestResponse<MerchantResponse> createMerchant(@Valid @RequestBody MerchantCreateRequest request){
        RestResponse<MerchantResponse> res = new RestResponse<>();
        res.setData(merchantService.handleCreateMerchant(request));
        return res;
    }
}
