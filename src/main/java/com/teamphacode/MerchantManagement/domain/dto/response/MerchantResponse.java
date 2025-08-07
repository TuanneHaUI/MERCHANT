package com.teamphacode.MerchantManagement.domain.dto.response;

import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class MerchantResponse {
    private String merchantId;
    private String accountNo;
    private String fullName;
    private String shortName;
    private String mcc;
    private String city;
    private String location;
    private String phoneNo;
    private String email;
    private StatusEnum status;
    private LocalDate openDate;
    private LocalDate closeDate;
    private String branchCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}