package com.teamphacode.MerchantManagement.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String merchantId;

    private String accountNo;

    private LocalDateTime changedAt;

    private String changedBy;

    @Column(name = "change_content", length = 3000)
    private String changeContent;
    @Column(name = "reason", length = 5000)
    private String reason;
}
