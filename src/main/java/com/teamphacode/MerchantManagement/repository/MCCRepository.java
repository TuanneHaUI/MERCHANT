package com.teamphacode.MerchantManagement.repository;

import com.teamphacode.MerchantManagement.domain.MCC;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MCCRepository extends JpaRepository<MCC, Long> {
    MCC findByCode( String code);
}
