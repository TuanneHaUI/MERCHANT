package com.teamphacode.MerchantManagement.service;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqUpdateMerchant;
import com.teamphacode.MerchantManagement.domain.dto.response.*;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;


public interface MerchantService {

    MerchantResponse handleCreateMerchant(MerchantCreateRequest request);

    Merchant handleUpdateMerchant(ReqUpdateMerchant reqUpdateMerchant) throws IdInvalidException;

    ResultPaginationDTO handleReportMerchantByStatus(StatusEnum statusEnum, Pageable pageable) throws IdInvalidException;

    Merchant findMerchantByAccountNo(String accountNo);

    boolean isChanged(Object oldVal, Object newVal);

    List<ResMerchantYearStatusDTO> handleCountMerchantByYear(int year);

    ResultPaginationDTO handleFindByMerchantIdAndAccountNoAndStatus(String merchantId, String accountNo, StatusEnum status, Pageable pageable);

    List<MerchantTransactionSummaryDTO> handleCountTransactionByMerchant(LocalDateTime fromDate, LocalDateTime toDate);

    List<TransactionReportDTO> handleFindTransactionsByMerchant(String merchantId, LocalDateTime fromDate, LocalDateTime toDate);

    byte[] exportMerchantYearReport(int year, List<ResMerchantYearStatusDTO> data) throws IOException;

    byte[] exportMerchantTransactionReport(LocalDateTime fromDate, LocalDateTime toDate, List<MerchantTransactionSummaryDTO> data) throws IOException;

}


