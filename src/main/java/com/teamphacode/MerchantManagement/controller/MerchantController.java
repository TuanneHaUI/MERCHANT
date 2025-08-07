package com.teamphacode.MerchantManagement.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
import com.teamphacode.MerchantManagement.service.MerchantService;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MerchantController {
    private final MerchantService merchantService;

    @GetMapping("/merchants/report-by-status")
    public ResultPaginationDTO reportByStatus(@RequestParam("status") StatusEnum statusEnum, Pageable pageable) throws IdInvalidException {
        return this.merchantService.handleReportMerchantByStatus(statusEnum, pageable);
    }
}
