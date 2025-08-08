package com.teamphacode.MerchantManagement.controller;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqUpdateMerchant;
import com.teamphacode.MerchantManagement.domain.dto.response.MerchantResponse;
import com.teamphacode.MerchantManagement.domain.dto.response.ResMerchantYearStatusDTO;
import com.teamphacode.MerchantManagement.domain.dto.response.RestResponse;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.service.MerchantService;
import com.teamphacode.MerchantManagement.service.impl.MerchantServiceImpl;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

     @GetMapping("/merchants/report-by-status")
     public ResponseEntity<ResultPaginationDTO> reportByStatus(@RequestParam("status") StatusEnum statusEnum, Pageable pageable) throws IdInvalidException {
         return ResponseEntity.ok(this.merchantService.handleReportMerchantByStatus(statusEnum, pageable));
     }

     @GetMapping("/merchants/count-active-by-year")
     public ResponseEntity<List<ResMerchantYearStatusDTO>> countMerchantActiveByYear(@RequestParam("year") int year){
         return ResponseEntity.ok(this.merchantService.handleCountMerchantActiveByYear(year));
     }

     @GetMapping("/merchants/search")
    public ResponseEntity<ResultPaginationDTO> findByMerchantIdAndAccountNoAndStatus( @RequestParam(required = false) String merchantId,
                                                                                      @RequestParam(required = false) String accountNo,
                                                                                      @RequestParam(required = false) StatusEnum status,
                                                                                      Pageable pageable){
        return ResponseEntity.ok(this.merchantService.handleFindByMerchantIdAndAccountNoAndStatus(merchantId, accountNo, status,pageable));
     }
}
