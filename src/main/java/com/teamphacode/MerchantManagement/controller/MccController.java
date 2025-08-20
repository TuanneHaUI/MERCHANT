package com.teamphacode.MerchantManagement.controller;

import com.teamphacode.MerchantManagement.domain.Mcc;
import com.teamphacode.MerchantManagement.domain.dto.request.MccUpdateRequest;
import com.teamphacode.MerchantManagement.domain.dto.response.RestResponse;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.service.impl.MccServiceImpl;
import com.teamphacode.MerchantManagement.service.impl.MerchantHistoryServiceImpl;
import com.teamphacode.MerchantManagement.util.LogUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MccController {
    private static final Logger logger = LoggerFactory.getLogger(MccServiceImpl.class);
    @Autowired
    private MccServiceImpl mccService;

    @PostMapping("/mcc/createMcc")
    public ResponseEntity<?> createMcc(
            @RequestParam String requestId,
            @RequestParam String requestTime,
            @Valid @RequestBody Mcc request) {
        logger.info("requestBody: "+ requestId + " requestTime: " + requestTime + " data: " +  request);
        Mcc createdMcc = mccService.createMcc(request);
        LogUtil.logJsonResponse(logger, HttpStatus.OK, createdMcc);
        return ResponseEntity.ok(createdMcc);
    }

    @PutMapping("/mcc/updateMcc/{code}")
    public ResponseEntity<?> updateMcc(
            @RequestParam String requestId,
            @RequestParam String requestTime,
            @PathVariable String code,
            @Valid @RequestBody MccUpdateRequest request) {
        logger.info("requestBody: "+ requestId + " requestTime: " + requestTime + " data: " +  request);
        Mcc updatedMcc = mccService.updateMcc(code, request);
        LogUtil.logJsonResponse(logger, HttpStatus.OK, updatedMcc);
        return ResponseEntity.ok(updatedMcc);
    }

    @GetMapping("/mcc/getAllMcc")
    public ResponseEntity<?> getAllMccs(Pageable pageable) {
        ResultPaginationDTO result = mccService.getAllMccs(pageable);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/mcc/removeMcc/{code}")
    public ResponseEntity<?> removeMcc(
            @RequestParam String requestId,
            @RequestParam String requestTime,
            @PathVariable String code){
        logger.info("requestBody: "+ requestId + " requestTime: " + requestTime + " data: code " +  code);
        mccService.deleteMcc(code);
        LogUtil.logJsonResponse(logger, HttpStatus.OK, "deleted");
        return ResponseEntity.ok("Xóa Mcc " + code + "thành công");
    }
}
