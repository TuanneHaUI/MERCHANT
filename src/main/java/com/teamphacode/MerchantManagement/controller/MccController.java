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

@RestController
@RequestMapping("/api/v1")
public class MccController {
    @Autowired
    private MccServiceImpl mccService;

    @PostMapping("/mcc/createMcc")
    public ResponseEntity<RestResponse<Mcc>> createMcc(@Valid @RequestBody Mcc request) {
        Mcc createdMcc = mccService.createMcc(request);
        return new ResponseEntity<>(
                RestResponse.<Mcc>builder()
                        .errorCode(0)
                        .errorDesc("Thêm mới MCC thành công!")
                        .data(createdMcc)
                        .build(),
                HttpStatus.CREATED
        );
    }

    // <<< SỬA LẠI UPDATE
    @PutMapping("/mcc/updateMcc/{code}")
    public ResponseEntity<RestResponse<Mcc>> updateMcc(
            @PathVariable String code,
            @Valid @RequestBody MccUpdateRequest request) {
        Mcc updatedMcc = mccService.updateMcc(code, request);
        return ResponseEntity.ok(
                RestResponse.<Mcc>builder()
                        .errorCode(0)
                        .errorDesc("Cập nhật MCC thành công!")
                        .data(updatedMcc)
                        .build()
        );
    }

    // <<< SỬA LẠI GET ALL
    @GetMapping("/mcc/getAllMcc")
    public ResponseEntity<RestResponse<ResultPaginationDTO>> getAllMccs(Pageable pageable) {
        ResultPaginationDTO result = mccService.getAllMccs(pageable);
        return ResponseEntity.ok(
                RestResponse.<ResultPaginationDTO>builder()
                        .errorCode(0)
                        .data(result)
                        .build()
        );
    }

    // <<< SỬA LẠI DELETE
    @DeleteMapping("/mcc/removeMcc/{code}")
    public ResponseEntity<RestResponse<String>> removeMcc(@PathVariable String code){
        mccService.deleteMcc(code);
        return ResponseEntity.ok(
                RestResponse.<String>builder()
                        .errorCode(0)
                        .errorDesc("Xóa MCC thành công!")
                        .data(null) // Không cần data khi xóa
                        .build()
        );
    }
}