package com.teamphacode.MerchantManagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqUpdateMerchant;
import com.teamphacode.MerchantManagement.domain.dto.response.*;
import com.teamphacode.MerchantManagement.domain.dto.response.MerchantResponse;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.service.impl.MerchantHistoryServiceImpl;
import com.teamphacode.MerchantManagement.service.impl.MerchantServiceImpl;
import com.teamphacode.MerchantManagement.util.LogUtil;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1")
@Validated
public class MerchantController {
    @Autowired
    private MerchantServiceImpl merchantService;
    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);

    @DeleteMapping("/merchant/delete/{id}")
    ResponseEntity<?> deleteMerchant(
            @RequestParam String requestId,
            @RequestParam String requestTime,
            @PathVariable String id){
        logger.info("requestBody: "+ requestId + " requestTime: " + requestTime + " data: id " + id);
        merchantService.deleteMerchant(id);
        LogUtil.logJsonResponse(logger, HttpStatus.OK, "deleted complete");
        return ResponseEntity.ok(new ResImportExcel("deleted complete"));
    }


    @PostMapping("/merchant/create")
    ResponseEntity<MerchantResponse> createMerchant(
            @RequestParam String requestId,
            @RequestParam String requestTime,
            @Valid @RequestBody MerchantCreateRequest request){
        logger.info("requestBody: "+ requestId + " requestTime: " + requestTime + " data: " +  request );
        return ResponseEntity.ok(merchantService.handleCreateMerchant(request));
    }

    @PutMapping("/merchant/update")
    ResponseEntity<?> updateMerchant(@Valid @RequestBody ReqUpdateMerchant request) throws IdInvalidException {
        Merchant merchant = this.merchantService.handleUpdateMerchant(request);
        return ResponseEntity.ok(merchant);
    }

     @GetMapping("/merchants/report-by-status")
     public ResponseEntity<ResultPaginationDTO> reportByStatus(@RequestParam String requestId,
                                                               @RequestParam String requestTime,
                                                               @RequestParam("status") StatusEnum statusEnum, Pageable pageable) throws IdInvalidException {
         logger.info("requestBody: "+ requestId + " requestTime: " + requestTime + " data: " +  statusEnum );

         ResultPaginationDTO results = this.merchantService.handleReportMerchantByStatus(statusEnum, pageable);

         LogUtil.logJsonResponse(logger, HttpStatus.OK, results);

         return ResponseEntity.ok(results);
     }



    @GetMapping("/merchants/search")
    public ResponseEntity<ResultPaginationDTO> findByFilter(
            @RequestParam String requestId,
            @RequestParam String requestTime,
            @RequestParam(required = false) String filter,
            Pageable pageable) throws IdInvalidException{

        String merchantId = null;
        String accountNo = null;
        StatusEnum status = null;

        if (filter != null && !filter.isEmpty()) {
            // Tách filter theo 'and' (có thể có khoảng trắng xung quanh)
            String[] conditions = filter.split("\\s+and\\s+");

            for (String cond : conditions) {
                // cond ví dụ: "merchantId ~ 'MC000000002'"
                // Regex tách key ~ 'value'
                Pattern pattern = Pattern.compile("(\\w+)\\s*~\\s*'([^']*)'");
                Matcher matcher = pattern.matcher(cond);
                if (matcher.find()) {
                    String key = matcher.group(1);
                    String value = matcher.group(2);

                    switch (key) {
                        case "merchantId":
                            merchantId = value;
                            break;
                        case "accountNo":
                            accountNo = value;
                            break;
                        case "status":
                            try {
                                status = StatusEnum.valueOf(value);
                            } catch (IllegalArgumentException e) {
                                // xử lý status không hợp lệ nếu cần
                                throw new IdInvalidException("status không hợp lệ hãy truyền Active hoặc Close");
                            }
                            break;
                        default:
                            // key không hỗ trợ, có thể log hoặc bỏ qua
                            break;
                    }
                }
            }
        }
        logger.info("requestBody: "+ requestId + " requestTime: " + requestTime + " data: " +  merchantId +", " + accountNo+", " + status);
        ResultPaginationDTO results =  this.merchantService.handleFindByMerchantIdAndAccountNoAndStatus(merchantId, accountNo, status, pageable);

        LogUtil.logJsonResponse(logger, HttpStatus.OK, results);

        return ResponseEntity.ok(
                results
        );
    }



    @GetMapping("/merchants/summary-transaction-by-merchant")
    public ResponseEntity<List<MerchantTransactionSummaryDTO>> getTransactionSummary(
            @RequestParam String requestId,
            @RequestParam String requestTime,
            @RequestParam("fromDate") LocalDateTime fromDate,
            @RequestParam("toDate") LocalDateTime toDate) {
        logger.info("requestBody: "+ requestId + " requestTime: " + requestTime + " data: " +  fromDate +", " + toDate);

        List<MerchantTransactionSummaryDTO> results = this.merchantService.handleCountTransactionByMerchant(fromDate, toDate);

        LogUtil.logJsonResponse(logger, HttpStatus.OK, results);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/merchants/fetch-transaction/{merchantId}")
    public ResponseEntity<List<TransactionReportDTO>> getTransactionsByMerchant(
            @PathVariable String merchantId,
            @RequestParam LocalDateTime fromDate,
            @RequestParam LocalDateTime toDate,
            @RequestParam String requestId,
            @RequestParam String requestTime
    ) throws IdInvalidException{
        logger.info("requestBody: "+ requestId + " requestTime: " + requestTime + " data: " + merchantId +", "+ fromDate +", " + toDate);

        List<TransactionReportDTO> results = this.merchantService.handleFindTransactionsByMerchant(merchantId, fromDate, toDate);

        LogUtil.logJsonResponse(logger, HttpStatus.OK, results);

        return ResponseEntity.ok(this.merchantService.handleFindTransactionsByMerchant(merchantId, fromDate, toDate));
    }

    @GetMapping("/merchants/count-merchant-by-year")
    public ResponseEntity<List<ResMerchantYearStatusDTO>> countMerchantByYear(
            @RequestParam String requestId,
            @RequestParam String requestTime,
            @RequestParam("year") int year){
        logger.info("requestBody: "+ requestId + " requestTime: " + requestTime + " data: " +  year);

        List<ResMerchantYearStatusDTO> results = this.merchantService.handleCountMerchantByYear(year);
        LogUtil.logJsonResponse(logger, HttpStatus.OK, results);
        return ResponseEntity.ok(results);
    }

        @GetMapping("/merchants/export-merchant-year")
        public ResponseEntity<byte[]> downloadMerchantYearReport(@RequestParam String requestId,
                                                                 @RequestParam String requestTime,
                                                                 @RequestParam int year) throws IOException {

            logger.info("requestBody: "+ requestId + " requestTime: " + requestTime + " data: " +  year);

            List<ResMerchantYearStatusDTO> data = this.merchantService.handleCountMerchantByYear(year);

            LogUtil.logJsonResponseService(logger, data, "List<ResMerchantYearStatusDTO> data: ");

            byte[] excelFile = this.merchantService.handleExportMerchantByYear(year, data);

            if(excelFile != null){
                logger.info("respose: tạo excel thành công");
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merchant_year_" + year + ".xlsx")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelFile);
        }

    @GetMapping("/merchants/export-transactionSummary")
    public ResponseEntity<byte[]> downloadTransactionSummary(@RequestParam String requestId,
                                                             @RequestParam String requestTime,
                                                             @RequestParam("fromDate")  LocalDateTime fromDate,
                                                             @RequestParam("toDate") LocalDateTime toDate) throws IOException {
        logger.info("requestBody: "+ requestId + " requestTime: " + requestTime + " data: " + fromDate +", " + toDate);

        List<MerchantTransactionSummaryDTO> data = this.merchantService.handleCountTransactionByMerchant(fromDate, toDate);
        LogUtil.logJsonResponseService(logger, data, "List<MerchantTransactionSummaryDTO> data: ");

        byte[] excelFile = this.merchantService.handleExportTransactionSummary(fromDate, toDate, data);
        if(excelFile != null){
            logger.info("respose: tạo excel thành công");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merchant_year_" + toDate + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile);
    }

    @GetMapping("/merchants/export-transactionDetail/{merchantId}")
    public ResponseEntity<byte[]> downloadTransactionDetail(@PathVariable String merchantId,
                                                            @RequestParam("fromDate")  LocalDateTime fromDate,
                                                             @RequestParam("toDate") LocalDateTime toDate,@RequestParam String requestId,
                                                            @RequestParam String requestTime) throws IOException, IdInvalidException {
        logger.info("requestBody: "+ requestId + " requestTime: " + requestTime + " data: " + merchantId +", "+ fromDate +", " + toDate);
        List<TransactionReportDTO> data = this.merchantService.handleFindTransactionsByMerchant(merchantId, fromDate, toDate);
        LogUtil.logJsonResponseService(logger, data, "List<TransactionReportDTO> data: ");

        byte[] excelFile = this.merchantService.handleExportTransactionDetailByMerchant(fromDate, toDate, data);
        if(excelFile != null){
            logger.info("respose: tạo excel thành công");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merchant_year_" + toDate + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile);
    }

    @GetMapping("/merchants")
    public ResponseEntity<ResultPaginationDTO> fetchMerchants(Pageable pageable){
        return ResponseEntity.ok(this.merchantService.handleFetchMerchants(pageable));
    }
}
