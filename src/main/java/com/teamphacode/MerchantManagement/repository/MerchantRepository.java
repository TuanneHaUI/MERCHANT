package com.teamphacode.MerchantManagement.repository;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String> {
    Boolean existsByAccountNo(String accountNo);
     Page<Merchant> findByStatus(StatusEnum status, Pageable pageable);
}