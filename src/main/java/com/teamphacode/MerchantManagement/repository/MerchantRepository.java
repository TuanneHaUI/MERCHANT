package com.teamphacode.MerchantManagement.repository;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String> {
    Boolean existsByAccountNo(String accountNo);
    Page<Merchant> findByStatus(StatusEnum status, Pageable pageable);
    @Query("SELECT m.accountNo FROM Merchant m WHERE m.accountNo IN :accountNos")
    Set<String> findExistingAccountNos(@Param("accountNos") Set<String> accountNos);

    @Query("SELECT m.merchantId FROM Merchant m WHERE m.merchantId IN :merchantIds")
    Set<String> findExistingMerchantIds(@Param("merchantIds") Set<String> merchantIds);
}