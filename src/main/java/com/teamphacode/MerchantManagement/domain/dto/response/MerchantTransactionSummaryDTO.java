package com.teamphacode.MerchantManagement.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MerchantTransactionSummaryDTO {
    private String accountNo;
    private String merchantId;
    private String shortName;
    private Long successCount;
    private Long failedCount;
    private Long timeoutCount;
    private Long totalCount;
}
