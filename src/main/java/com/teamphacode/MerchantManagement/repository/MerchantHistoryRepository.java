package com.teamphacode.MerchantManagement.repository;

import com.teamphacode.MerchantManagement.domain.MerchantHistory;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MerchantHistoryRepository extends JpaRepository<MerchantHistory, Long>, JpaSpecificationExecutor<MerchantHistory> {
    @Query("SELECT m FROM MerchantHistory m " +
            "WHERE (:merchantId IS NULL OR m.merchantId = :merchantId) " +
            "AND (:accountNo IS NULL OR m.accountNo = :accountNo)")
    List<MerchantHistory> searchHistory(@Param("merchantId") String merchantId,
                                        @Param("accountNo") String accountNo);
}
