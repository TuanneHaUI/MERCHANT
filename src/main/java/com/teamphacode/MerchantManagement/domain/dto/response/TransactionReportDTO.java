package com.teamphacode.MerchantManagement.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionReportDTO {
    private String coreRef;
    private String transactionRef; // DE#63
    private String traceNo;        // DE#11
    private LocalDateTime transactionDate; // DE#15
    private String status;         // DE#39
    private String senderAccount;  // DE#102
    private String senderBank;     // DE#32
    private String receiverAccount; // DE#103
}