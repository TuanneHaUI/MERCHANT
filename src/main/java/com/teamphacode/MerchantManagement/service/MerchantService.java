package com.teamphacode.MerchantManagement.service;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.MerchantHistory;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqUpdateMerchant;
import com.teamphacode.MerchantManagement.domain.dto.response.*;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;


public interface MerchantService  {

    MerchantResponse handleCreateMerchant(MerchantCreateRequest request);

    List<Merchant> getAll();

    void handleCreateMultipleMerchants(List<MerchantCreateRequest> requests);

    Merchant handleUpdateMerchant(ReqUpdateMerchant reqUpdateMerchant) throws IdInvalidException;

    ResultPaginationDTO handleReportMerchantByStatus(StatusEnum statusEnum, Pageable pageable) throws IdInvalidException;

    Merchant findMerchantByAccountNo(String accountNo);

    boolean isChanged(Object oldVal, Object newVal);

    List<ResMerchantYearStatusDTO> handleCountMerchantByYear(int year);

    ResultPaginationDTO handleFindByMerchantIdAndAccountNoAndStatus(String merchantId, String accountNo, StatusEnum status, Pageable pageable);

    List<MerchantTransactionSummaryDTO> handleCountTransactionByMerchant(LocalDateTime fromDate, LocalDateTime toDate);

    List<TransactionReportDTO> handleFindTransactionsByMerchant(String merchantId, LocalDateTime fromDate, LocalDateTime toDate) throws IdInvalidException;

    byte[] handleExportMerchantByYear(int year, List<ResMerchantYearStatusDTO> data) throws IOException;

    byte[] handleExportTransactionSummary(LocalDateTime fromDate, LocalDateTime toDate, List<MerchantTransactionSummaryDTO> data) throws IOException;

    byte[] handleExportTransactionDetailByMerchant(LocalDateTime fromDate, LocalDateTime toDate, List<TransactionReportDTO> data) throws IOException;

    ResultPaginationDTO handleFetchMerchants(Pageable pageable);

}




