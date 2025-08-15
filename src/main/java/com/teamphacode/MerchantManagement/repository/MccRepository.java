package com.teamphacode.MerchantManagement.repository;

import com.teamphacode.MerchantManagement.domain.Mcc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MccRepository extends JpaRepository<Mcc, String> {
    List<Mcc> findByIsActiveTrueOrderByCode();
}
