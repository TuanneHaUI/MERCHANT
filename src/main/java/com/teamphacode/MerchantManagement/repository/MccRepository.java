package com.teamphacode.MerchantManagement.repository;

import com.teamphacode.MerchantManagement.domain.Mcc;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MccRepository extends JpaRepository<Mcc, String> {
    List<Mcc> findByIsActiveTrueOrderByCode();
    Mcc findByCode(String code);

}
