package com.teamphacode.MerchantManagement.repository;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;


import java.util.List;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String>, JpaSpecificationExecutor<Merchant> {
    Boolean existsByAccountNo(String accountNo);
    Page<Merchant> findByStatus(StatusEnum status, Pageable pageable);
    Optional<Merchant> findByAccountNo(String accountNo);


     //count merchants active by year
    @Query(value = "SELECT YEAR(m.open_date) AS year, m.status AS status, COUNT(*) AS total " +
            "FROM merchants m " +
            "WHERE m.status = 'Active' " +
            "AND YEAR(m.open_date) = :year " +
            "GROUP BY YEAR(m.open_date), m.status", nativeQuery = true)
     List<Object[]> countByYearAndStatusActive(@Param("year") int year);

    //count merchants close by year
    @Query(value = "SELECT YEAR(m.open_date) AS year, m.status AS status, COUNT(*) AS total " +
            "FROM merchants m " +
            "WHERE m.status = 'Active' " +
            "AND YEAR(m.open_date) = :year " +
            "GROUP BY YEAR(m.close_date), m.status", nativeQuery = true)
    List<Object[]> countByYearAndStatusClose(@Param("year") int year);

    //count transactions by merchant
    @Query(value = """
    SELECT 
        m.account_no AS accountNo,
        m.merchant_id AS merchantId,
        m.short_name AS shortName,
        SUM(CASE WHEN t.status = 'THANH_CONG' THEN 1 ELSE 0 END) AS successCount,
        SUM(CASE WHEN t.status = 'THAT_BAI' THEN 1 ELSE 0 END) AS failedCount,
        SUM(CASE WHEN t.status = 'TIME_OUT' THEN 1 ELSE 0 END) AS timeoutCount,
        COUNT(t.id) AS totalCount
    FROM transactions t
    JOIN merchants m ON t.merchant_id = m.merchant_id
    WHERE t.transaction_date BETWEEN :fromDate AND :toDate
    GROUP BY m.account_no, m.merchant_id, m.short_name
    ORDER BY m.account_no
    """, nativeQuery = true)
    List<Object[]> summarizeTransactionByMerchant(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}