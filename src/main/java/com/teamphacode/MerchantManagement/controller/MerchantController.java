package com.teamphacode.MerchantManagement.controller;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqUpdateMerchant;
import com.teamphacode.MerchantManagement.domain.dto.response.*;
import com.teamphacode.MerchantManagement.repository.MerchantRepository;
import com.teamphacode.MerchantManagement.service.MerchantService;
import com.teamphacode.MerchantManagement.domain.dto.response.MerchantResponse;
import com.teamphacode.MerchantManagement.domain.dto.response.ResMerchantYearStatusDTO;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.service.impl.MerchantServiceImpl;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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
    public ResponseEntity<?> updateMerchant(@Valid @RequestBody ReqUpdateMerchant request) throws IdInvalidException {

        Merchant merchant = this.merchantService.handleUpdateMerchant(request);
        return ResponseEntity.ok(merchant);
    }

     @GetMapping("/merchants/report-by-status")
     public ResponseEntity<ResultPaginationDTO> reportByStatus(@RequestParam("status") StatusEnum statusEnum, Pageable pageable) throws IdInvalidException {
         return ResponseEntity.ok(this.merchantService.handleReportMerchantByStatus(statusEnum, pageable));
     }


     @GetMapping("/merchants/count-by-year")
     public ResponseEntity<List<ResMerchantYearStatusDTO>> countMerchantActiveByYear(@RequestParam("year") int year){
         return ResponseEntity.ok(this.merchantService.handleCountMerchantByYear(year));
     }

     @GetMapping("/merchants/search")
    public ResponseEntity<ResultPaginationDTO> findByMerchantIdAndAccountNoAndStatus( @RequestParam(required = false) String merchantId,
                                                                                      @RequestParam(required = false) String accountNo,
                                                                                      @RequestParam(required = false) StatusEnum status,
                                                                                      Pageable pageable){
        return ResponseEntity.ok(this.merchantService.handleFindByMerchantIdAndAccountNoAndStatus(merchantId, accountNo, status,pageable));
     }

    @GetMapping("/merchants/summary-transaction-by-merchant")
    public ResponseEntity<List<MerchantTransactionSummaryDTO>> getTransactionSummary(
            @RequestParam("fromDate")  LocalDateTime fromDate,
            @RequestParam("toDate") LocalDateTime toDate) {

        return ResponseEntity.ok(this.merchantService.handleCountTransactionByMerchant(fromDate, toDate));
    }

    @GetMapping("/merchants/fetch-transaction/{merchantId}")
    public ResponseEntity<List<TransactionReportDTO>> getTransactionsByMerchant(
            @PathVariable String merchantId,
            @RequestParam LocalDateTime fromDate,
            @RequestParam LocalDateTime toDate
    ) throws IdInvalidException{
        return ResponseEntity.ok(this.merchantService.handleFindTransactionsByMerchant(merchantId, fromDate, toDate));
    }

    @GetMapping("/merchants/export-merchant-year")
    public ResponseEntity<byte[]> downloadMerchantYearReport(@RequestParam int year) throws IOException {
        List<ResMerchantYearStatusDTO> data = this.merchantService.handleCountMerchantByYear(year);

        byte[] excelFile = this.merchantService.handleExportMerchantByYear(year, data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merchant_year_" + year + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile);
    }

    @GetMapping("/merchants/export-transactionSummary")
    public ResponseEntity<byte[]> downloadTransactionSummary(@RequestParam("fromDate")  LocalDateTime fromDate,
                                                             @RequestParam("toDate") LocalDateTime toDate) throws IOException {
        List<MerchantTransactionSummaryDTO> data = this.merchantService.handleCountTransactionByMerchant(fromDate, toDate);

        byte[] excelFile = this.merchantService.handleExportTransactionSummary(fromDate, toDate, data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merchant_year_" + toDate + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile);
    }

    @GetMapping("/merchants/export-transactionDetail/{merchantId}")
    public ResponseEntity<byte[]> downloadTransactionDetail( @PathVariable String merchantId, @RequestParam("fromDate")  LocalDateTime fromDate,
                                                             @RequestParam("toDate") LocalDateTime toDate) throws IOException, IdInvalidException {
        List<TransactionReportDTO> data = this.merchantService.handleFindTransactionsByMerchant(merchantId, fromDate, toDate);

        byte[] excelFile = this.merchantService.handleExportTransactionDetailByMerchant(fromDate, toDate, data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merchant_year_" + toDate + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile);
    }
}
