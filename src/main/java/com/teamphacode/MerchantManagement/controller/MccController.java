package com.teamphacode.MerchantManagement.controller;

import com.teamphacode.MerchantManagement.domain.Mcc;
import com.teamphacode.MerchantManagement.domain.dto.request.MccUpdateRequest;
import com.teamphacode.MerchantManagement.domain.dto.response.RestResponse;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.service.impl.MccServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MccController {
    @Autowired
    private MccServiceImpl mccService;

    @PostMapping("/mcc/createMcc")
    public ResponseEntity<?> createMcc(@Valid @RequestBody Mcc request) {
        Mcc createdMcc = mccService.createMcc(request);
        return ResponseEntity.ok(createdMcc);
    }

    @PutMapping("/mcc/updateMcc/{code}")
    public ResponseEntity<?> updateMcc(
            @PathVariable String code,
            @Valid @RequestBody MccUpdateRequest request) {
        Mcc updatedMcc = mccService.updateMcc(code, request);
        return ResponseEntity.ok(updatedMcc);
    }

    @GetMapping("/mcc/getAllMcc")
    public ResponseEntity<?> getAllMccs(Pageable pageable) {
        ResultPaginationDTO result = mccService.getAllMccs(pageable);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/mcc/removeMcc/{code}")
    public ResponseEntity<?> removeMcc(@PathVariable String code){
        mccService.deleteMcc(code);
        return ResponseEntity.ok("Xóa Mcc " + code + "thành công");
    }
}
