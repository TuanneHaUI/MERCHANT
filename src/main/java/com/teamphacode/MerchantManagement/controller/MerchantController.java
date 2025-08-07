package com.teamphacode.MerchantManagement.controller;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqUpdateMerchant;
import com.teamphacode.MerchantManagement.domain.dto.response.MerchantResponse;
import com.teamphacode.MerchantManagement.domain.dto.response.RestResponse;
import com.teamphacode.MerchantManagement.service.MerchantService;
import com.teamphacode.MerchantManagement.service.impl.MerchantServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Validated
public class MerchantController {
    @Autowired
    private MerchantServiceImpl merchantService;

    @PostMapping("/merchant/create")
    ResponseEntity<MerchantResponse> createMerchant(@Valid @RequestBody MerchantCreateRequest request){
        return ResponseEntity.ok(merchantService.handleCreateMerchant(request));
    }

    @PutMapping("/merchant/update")
    ResponseEntity<?> updateMerchant(@Valid @RequestBody ReqUpdateMerchant request){
        Merchant merchant = this.merchantService.handleUpdateMerchant(request);
        return ResponseEntity.ok(merchant);
    }


// package com.teamphacode.MerchantManagement.controller;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.teamphacode.MerchantManagement.domain.Merchant;
// import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
// import com.teamphacode.MerchantManagement.service.MerchantService;
// import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
// import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequiredArgsConstructor
// @RequestMapping("/api/v1")
// public class MerchantController {
//     private final MerchantService merchantService;

//     @GetMapping("/merchants/report-by-status")
//     public ResultPaginationDTO reportByStatus(@RequestParam("status") StatusEnum statusEnum, Pageable pageable) throws IdInvalidException {
//         return this.merchantService.handleReportMerchantByStatus(statusEnum, pageable);
//     }
// }


}
