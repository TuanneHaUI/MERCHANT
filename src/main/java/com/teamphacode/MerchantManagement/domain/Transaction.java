package com.teamphacode.MerchantManagement.domain;

import com.teamphacode.MerchantManagement.util.constant.TransactionStatusEnum;
import com.teamphacode.MerchantManagement.util.constant.TransactionTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.teamphacode.MerchantManagement.util.SecurityUtil;


import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Merchant merchant;


    @Column(name = "core_ref", length = 50)
    private String coreRef;


    @Column(name = "transaction_ref", length = 50)
    private String transactionRef;

    // Trace number - DE#11
    @Column(name = "trace_no", length = 20)
    private String traceNo;


    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatusEnum status;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionTypeEnum type;


    @Column(name = "amount", nullable = false)
    private long amount;


    @Column(name = "sender_account", length = 30)
    private String senderAccount;


    @Column(name = "sender_bank", length = 30)
    private String senderBank;


    @Column(name = "receiver_account", length = 30)
    private String receiverAccount;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
