package com.teamphacode.MerchantManagement.repository;

import com.teamphacode.MerchantManagement.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByEmail(String email);

    boolean existsByEmail(String email);

    Users findByRefreshTokenAndEmail(String refreshToken, String email);
}
