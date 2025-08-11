package com.teamphacode.MerchantManagement.repository;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.response.TransactionReportDTO;
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


     //count merchants by year
     @Query(value = """
    SELECT 
        m.status,
        SUM(CASE WHEN MONTH(CASE WHEN m.status = 'Close' THEN m.close_date ELSE m.open_date END) = 1 THEN 1 ELSE 0 END) AS Thang01,
        SUM(CASE WHEN MONTH(CASE WHEN m.status = 'Close' THEN m.close_date ELSE m.open_date END) = 2 THEN 1 ELSE 0 END) AS Thang02,
        SUM(CASE WHEN MONTH(CASE WHEN m.status = 'Close' THEN m.close_date ELSE m.open_date END) = 3 THEN 1 ELSE 0 END) AS Thang03,
        SUM(CASE WHEN MONTH(CASE WHEN m.status = 'Close' THEN m.close_date ELSE m.open_date END) = 4 THEN 1 ELSE 0 END) AS Thang04,
        SUM(CASE WHEN MONTH(CASE WHEN m.status = 'Close' THEN m.close_date ELSE m.open_date END) = 5 THEN 1 ELSE 0 END) AS Thang05,
        SUM(CASE WHEN MONTH(CASE WHEN m.status = 'Close' THEN m.close_date ELSE m.open_date END) = 6 THEN 1 ELSE 0 END) AS Thang06,
        SUM(CASE WHEN MONTH(CASE WHEN m.status = 'Close' THEN m.close_date ELSE m.open_date END) = 7 THEN 1 ELSE 0 END) AS Thang07,
        SUM(CASE WHEN MONTH(CASE WHEN m.status = 'Close' THEN m.close_date ELSE m.open_date END) = 8 THEN 1 ELSE 0 END) AS Thang08,
        SUM(CASE WHEN MONTH(CASE WHEN m.status = 'Close' THEN m.close_date ELSE m.open_date END) = 9 THEN 1 ELSE 0 END) AS Thang09,
        SUM(CASE WHEN MONTH(CASE WHEN m.status = 'Close' THEN m.close_date ELSE m.open_date END) = 10 THEN 1 ELSE 0 END) AS Thang10,
        SUM(CASE WHEN MONTH(CASE WHEN m.status = 'Close' THEN m.close_date ELSE m.open_date END) = 11 THEN 1 ELSE 0 END) AS Thang11,
        SUM(CASE WHEN MONTH(CASE WHEN m.status = 'Close' THEN m.close_date ELSE m.open_date END) = 12 THEN 1 ELSE 0 END) AS Thang12
    FROM merchants m
    WHERE YEAR(CASE WHEN m.status = 'Close' THEN m.close_date ELSE m.open_date END) = :year
      AND m.status IN ('Active', 'Close')
    GROUP BY m.status
    ORDER BY FIELD(m.status, 'Active', 'Close')
""", nativeQuery = true)
     List<Object[]> countMerchantByYear(@Param("year") int year);


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

    // fetch transaction detail by merchant
    @Query("SELECT t.coreRef, t.transactionRef, t.traceNo, t.transactionDate, " +
            "t.status, t.senderAccount, t.senderBank, t.receiverAccount " +
            "FROM Transaction t " +
            "WHERE t.merchant.id = :merchantId " +
            "AND t.transactionDate BETWEEN :fromDate AND :toDate")
    List<Object[]> findTransactionsByMerchant(
            @Param("merchantId") String merchantId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

}