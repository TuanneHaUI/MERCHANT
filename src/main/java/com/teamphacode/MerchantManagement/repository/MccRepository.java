package com.teamphacode.MerchantManagement.repository;

import com.teamphacode.MerchantManagement.domain.Mcc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MccRepository extends JpaRepository<Mcc, String> {
    List<Mcc> findByIsActiveTrueOrderByCode();
}
