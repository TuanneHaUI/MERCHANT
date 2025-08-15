package com.teamphacode.MerchantManagement.controller;

import com.teamphacode.MerchantManagement.domain.Mcc;
import com.teamphacode.MerchantManagement.domain.dto.request.MccUpdateRequest;
import com.teamphacode.MerchantManagement.domain.dto.request.RestRequest;
import com.teamphacode.MerchantManagement.service.impl.MccServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<Mcc> createMcc(@Valid @RequestBody RestRequest<Mcc> request) {
        Mcc createdMcc = mccService.createMcc(request.getData());
        return new ResponseEntity<>(createdMcc, HttpStatus.CREATED);
    }

    @PutMapping("/mcc/updateMcc/{code}")
    public ResponseEntity<Mcc> updateMcc(
            @PathVariable String code,
            @Valid @RequestBody MccUpdateRequest request) {
        Mcc updatedMcc = mccService.updateMcc(code, request);
        return ResponseEntity.ok(updatedMcc);
    }

    @GetMapping("/mcc/getAllMcc/active")
    public ResponseEntity<List<Mcc>> getActiveMccs() {
        List<Mcc> activeMccs = mccService.getActiveMccs();
        return ResponseEntity.ok(activeMccs);
    }
}
