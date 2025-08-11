package com.teamphacode.MerchantManagement.service.impl;

import com.teamphacode.MerchantManagement.config.MerchantIdConfig;
import com.teamphacode.MerchantManagement.domain.MCC;
import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.MerchantHistory;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqUpdateMerchant;
import com.teamphacode.MerchantManagement.domain.dto.response.*;
import com.teamphacode.MerchantManagement.domain.dto.specification.MerchantSpecification;
import com.teamphacode.MerchantManagement.mapper.MerchantMapper;
import com.teamphacode.MerchantManagement.repository.MCCRepository;
import com.teamphacode.MerchantManagement.repository.MerchantHistoryRepository;
import com.teamphacode.MerchantManagement.repository.MerchantRepository;
import com.teamphacode.MerchantManagement.service.MerchantService;
import com.teamphacode.MerchantManagement.util.SecurityUtil;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import com.teamphacode.MerchantManagement.util.errors.AppException;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import com.teamphacode.MerchantManagement.util.form.ExcelTemplateHelper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.teamphacode.MerchantManagement.util.form.ExcelTemplateHelper.*;

@Service
public class MerchantServiceImpl implements MerchantService {
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantIdConfig merchantIdConfig;
    @Autowired
    private MCCRepository mccRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private static final String HASH_KEY = "active_merchants";
    @Autowired
    private MerchantHistoryRepository merchantHistoryRepository;

    @Override
    public MerchantResponse handleCreateMerchant(MerchantCreateRequest request){
        Merchant merchant = merchantMapper.toMerchant(request);
        merchant.setMerchantId(merchantIdConfig.generateMerchantId());
        if (merchantRepository.existsByAccountNo(request.getAccountNo())) {
            throw new AppException("accountNo bị trùng", 400);
        }
        return merchantMapper.toMerchantResponse(merchantRepository.save(merchant));
    }

    @Override
    public boolean isChanged(Object oldVal, Object newVal) {
        if (newVal == null) return false;
        return !newVal.equals(oldVal);
    }


    @Override
    @Transactional
    public Merchant handleUpdateMerchant(ReqUpdateMerchant reqUpdateMerchant) throws IdInvalidException {
        Merchant merchant = merchantRepository.findByAccountNo(reqUpdateMerchant.getAccountNo())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy merchant với số tài khoản: " + reqUpdateMerchant.getAccountNo()));

        if (reqUpdateMerchant.getStatus() == StatusEnum.Close && reqUpdateMerchant.getCloseDate() == null) {
            throw new RuntimeException("Khi cập nhật trạng thái sang ĐÓNG, phải nhập thời điểm kết thúc hoạt động (closeDate).");
        }

        StringBuilder changeContent = new StringBuilder();

        if (isChanged(merchant.getFullName(), reqUpdateMerchant.getFullName())) {
            changeContent.append(String.format("fullName: '%s' → '%s'; ", merchant.getFullName(), reqUpdateMerchant.getFullName()));
            merchant.setFullName(reqUpdateMerchant.getFullName());
        }
        if (isChanged(merchant.getShortName(), reqUpdateMerchant.getShortName())) {
            changeContent.append(String.format("shortName: '%s' → '%s'; ", merchant.getShortName(), reqUpdateMerchant.getShortName()));
            merchant.setShortName(reqUpdateMerchant.getShortName());
        }
        if (isChanged(merchant.getMcc(), reqUpdateMerchant.getMcc())) {
            changeContent.append(String.format("mcc: '%s' → '%s'; ", merchant.getMcc(), reqUpdateMerchant.getMcc()));
            merchant.setMcc(reqUpdateMerchant.getMcc());
        }
        if (isChanged(merchant.getCity(), reqUpdateMerchant.getCity())) {
            changeContent.append(String.format("city: '%s' → '%s'; ", merchant.getCity(), reqUpdateMerchant.getCity()));
            merchant.setCity(reqUpdateMerchant.getCity());
        }
        if (isChanged(merchant.getLocation(), reqUpdateMerchant.getLocation())) {
            changeContent.append(String.format("location: '%s' → '%s'; ", merchant.getLocation(), reqUpdateMerchant.getLocation()));
            merchant.setLocation(reqUpdateMerchant.getLocation());
        }
        if (isChanged(merchant.getPhoneNo(), reqUpdateMerchant.getPhoneNo())) {
            changeContent.append(String.format("phoneNo: '%s' → '%s'; ", merchant.getPhoneNo(), reqUpdateMerchant.getPhoneNo()));
            merchant.setPhoneNo(reqUpdateMerchant.getPhoneNo());
        }
        if (isChanged(merchant.getEmail(), reqUpdateMerchant.getEmail())) {
            changeContent.append(String.format("email: '%s' → '%s'; ", merchant.getEmail(), reqUpdateMerchant.getEmail()));
            merchant.setEmail(reqUpdateMerchant.getEmail());
        }
        if (isChanged(merchant.getOpenDate(), reqUpdateMerchant.getOpenDate())) {
            changeContent.append(String.format("openDate: '%s' → '%s'; ", merchant.getOpenDate(), reqUpdateMerchant.getOpenDate()));
            merchant.setOpenDate(reqUpdateMerchant.getOpenDate());
        }
        if (isChanged(merchant.getCloseDate(), reqUpdateMerchant.getCloseDate())) {
            changeContent.append(String.format("closeDate: '%s' → '%s'; ", merchant.getCloseDate(), reqUpdateMerchant.getCloseDate()));
            merchant.setCloseDate(reqUpdateMerchant.getCloseDate());
        }
        if (isChanged(merchant.getStatus(), reqUpdateMerchant.getStatus())) {
            changeContent.append(String.format("status: '%s' → '%s'; ", merchant.getStatus(), reqUpdateMerchant.getStatus()));
            merchant.setStatus(reqUpdateMerchant.getStatus());
        }

        if (!changeContent.toString().isBlank()) {
            if(reqUpdateMerchant.getReason() == null){
                throw new IdInvalidException("Reason null");
            }
            MerchantHistory history = MerchantHistory.builder()
                    .merchantId(merchant.getMerchantId())
                    .accountNo(merchant.getAccountNo())
                    .changedAt(LocalDateTime.now())
                    .changedBy(SecurityUtil.getCurrentUserLogin().isPresent() == true
                            ? SecurityUtil.getCurrentUserLogin().get()
                            : "")
                    .changeContent(changeContent.toString())
                    .reason(reqUpdateMerchant.getReason())
                    .build();
            Boolean exists = redisTemplate.hasKey("MerchantHistoryServiceImpl");
            if(Boolean.TRUE.equals(exists)){
                redisTemplate.delete("MerchantHistoryServiceImpl");
            }
            merchantHistoryRepository.save(history);
        }

        return merchantRepository.save(merchant);
    }


    @Override
    public ResultPaginationDTO handleReportMerchantByStatus(StatusEnum statusEnum, Pageable pageable)
             throws IdInvalidException {

         String key = "report_by_status_" + statusEnum.name() + "_page_" + pageable.getPageNumber() +
                 "_size_" + pageable.getPageSize() + "_sort_" + pageable.getSort().toString();

         List<Merchant> cacheMerchants = (List<Merchant>) redisTemplate.opsForHash().get(HASH_KEY, key);
         Page<Merchant> merchantPage;

         if (cacheMerchants != null) {
             merchantPage = new PageImpl<>(cacheMerchants, pageable, cacheMerchants.size());
         } else {
             merchantPage = merchantRepository.findByStatus(statusEnum, pageable);
             if (merchantPage.isEmpty()) {
                 throw new IdInvalidException("không có dữ liệu của merchant với trạng thái " + statusEnum);
             }
             redisTemplate.opsForHash().put(HASH_KEY, key, merchantPage.getContent());
             redisTemplate.expire(HASH_KEY, Duration.ofHours(1));
         }

         // Tạo ResultPaginationDTO
         ResultPaginationDTO dto = new ResultPaginationDTO();
         ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

         meta.setPage(pageable.getPageNumber() + 1);
         meta.setPageSize(pageable.getPageSize());
         meta.setPages(merchantPage.getTotalPages());
         meta.setTotal(merchantPage.getTotalElements());

         dto.setMeta(meta);
         dto.setResult(merchantPage.getContent());

         return dto;
     }

    @Override
    public List<ResMerchantYearStatusDTO> handleCountMerchantByYear(int year) {
        List<Object[]> results = this.merchantRepository.countMerchantByYear(year);
        return results.parallelStream().map(row -> new ResMerchantYearStatusDTO(
                (String) row[0],
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue(),
                ((Number) row[4]).longValue(),
                ((Number) row[5]).longValue(),
                ((Number) row[6]).longValue(),
                ((Number) row[7]).longValue(),
                ((Number) row[8]).longValue(),
                ((Number) row[9]).longValue(),
                ((Number) row[10]).longValue(),
                ((Number) row[11]).longValue(),
                ((Number) row[12]).longValue()
        )).collect(Collectors.toList());
    }

    @Override
    public ResultPaginationDTO handleFindByMerchantIdAndAccountNoAndStatus(String merchantId, String accountNo, StatusEnum status, Pageable pageable) {
        Specification<Merchant> spec = MerchantSpecification.filter(merchantId, accountNo, status);
        Page<Merchant> merchantPage = this.merchantRepository.findAll(spec, pageable);

        ResultPaginationDTO dto = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(merchantPage.getTotalPages());
        meta.setTotal(merchantPage.getTotalElements());

        dto.setMeta(meta);
        dto.setResult(merchantPage.getContent());

         return dto;
     }

    @Override
    public List<MerchantTransactionSummaryDTO> handleCountTransactionByMerchant(LocalDateTime fromDate, LocalDateTime toDate) {

        List<Object[]> result = this.merchantRepository.summarizeTransactionByMerchant(fromDate, toDate);
        return result.parallelStream().map(row -> new MerchantTransactionSummaryDTO(
                (String) row[0],
                (String) row[1],
                (String) row[2],
                ((Number) row[3]).longValue(),
                ((Number) row[4]).longValue(),
                ((Number) row[5]).longValue(),
                ((Number) row[6]).longValue()
        )).collect(Collectors.toList());
    }

    @Override
    public List<TransactionReportDTO> handleFindTransactionsByMerchant(String merchantId, LocalDateTime fromDate, LocalDateTime toDate) {
        List<Object[]> result = this.merchantRepository.findTransactionsByMerchant(merchantId, fromDate, toDate);

        return result.parallelStream()
                .map(row -> new TransactionReportDTO(
                        (String) row[0],
                        (String) row[1],
                        (String) row[2],
                        (LocalDateTime) row[3],
                        row[4] != null ? row[4].toString() : null,
                        (String) row[5],
                        (String) row[6],
                        (String) row[7]
                ))
                .collect(Collectors.toList());    }

    @Override
    public Merchant findMerchantByAccountNo(String accountNo) {
        return null;
    }

    @Override
    public byte[] exportMerchantYearReport(int year, List<ResMerchantYearStatusDTO> data) throws IOException {
        Workbook workbook = ExcelTemplateHelper.createWorkbook();
        Sheet sheet = workbook.createSheet("Merchant Year Report");

        // ===== 1. Tiêu đề =====
        ExcelTemplateHelper.addTitle(sheet, "BÁO CÁO TỔNG HỢP DỮ LIỆU", 13);

        // ===== 2. Thông tin Năm và Ngày lấy báo cáo =====
        ExcelTemplateHelper.addInfoRow(sheet, "Năm:", String.valueOf(year), 2);
        ExcelTemplateHelper.addInfoRow(sheet, "Ngày lấy báo cáo:", LocalDate.now().toString(), 3);

        // ===== 3. Header =====
        List<String> headers = new ArrayList<>();
        headers.add("STT");
        headers.add("LOẠI MERCHANT");
        for (int i = 1; i <= 12; i++) {
            headers.add(String.format("THÁNG %02d", i));
        }

        ExcelTemplateHelper.addHeaderRow(sheet, headers, 5);

        // ===== 4. Data =====
        List<List<Object>> rows = new ArrayList<>();
        int stt = 1;
        for (ResMerchantYearStatusDTO dto : data) {
            rows.add(List.of(
                    stt++,
                    dto.getStatus(),
                    dto.getThang01(), dto.getThang02(), dto.getThang03(), dto.getThang04(),
                    dto.getThang05(), dto.getThang06(), dto.getThang07(), dto.getThang08(),
                    dto.getThang09(), dto.getThang10(), dto.getThang11(), dto.getThang12()
            ));
        }
        ExcelTemplateHelper.addDataRows(sheet, rows, 6);

        // ===== 5. Dòng Tổng cộng =====
        int startDataRow = 7; // Excel tính từ 1, nhưng ở đây STT đầu tiên nằm ở row index 6 => Excel row 7
        int totalRowIndex = 6 + rows.size();
        addTotalRow(sheet, "Tổng cộng:", 0, 1, 2, 13, startDataRow, totalRowIndex);

        // ===== 6. Xuất ra byte[] =====
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    public byte[] exportMerchantTransactionReport(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            List<MerchantTransactionSummaryDTO> data
    ) throws IOException {

        Workbook workbook = ExcelTemplateHelper.createWorkbook();;
        Sheet sheet = workbook.createSheet("Merchant Report");

        // ===== Tiêu đề =====
        ExcelTemplateHelper.addTitle(sheet, "TỔNG HỢP GIAO DỊCH THEO MERCHANT", 7);

        // ===== Thông tin ngày =====
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        ExcelTemplateHelper.addInfoRow(sheet, "Từ ngày:", df.format(fromDate), 2);
        ExcelTemplateHelper.addInfoRow(sheet, "Đến ngày:", df.format(toDate), 3);
        ExcelTemplateHelper.addInfoRow(sheet, "Ngày lấy báo cáo:", df.format(LocalDateTime.now()), 4);

        // ===== Header nhóm =====
        List<Object[]> headers = List.of(
                new Object[]{0, 1, "STT"},
                new Object[]{1, 3, "THÔNG TIN MERCHANT"},
                new Object[]{4, 4, "THÔNG TIN GIAO DỊCH"}
        );

        ExcelTemplateHelper.createGroupHeader(sheet, workbook, 6, headers);

        // ===== Header con =====
        Row subHeader = sheet.createRow(7);
        String[] header = {"SỐ TK", "MÃ MERCHANT", "TÊN TẮT", "THÀNH CÔNG", "THẤT BẠI", "TIMEOUT", "TỔNG"};

        CellStyle headerStyle = ExcelTemplateHelper.createHeaderStyle(workbook);
        for (int i = 0; i < header.length; i++) {
            Cell cell = subHeader.createCell(i + 1); // lưu ý +1 vì cột 0 là STT nhóm header
            cell.setCellValue(header[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i + 1);
        }

        // ===== Dữ liệu =====
        List<List<Object>> rows = new ArrayList<>();
        int stt = 1;
        for (MerchantTransactionSummaryDTO dto : data) {
            rows.add(List.of(
                    stt++,
                    dto.getAccountNo(), dto.getMerchantId(), dto.getShortName(), dto.getSuccessCount(), dto.getFailedCount(),dto.getTimeoutCount(),
                    dto.getTotalCount()
            ));
        }
        ExcelTemplateHelper.addDataRows(sheet, rows, 8);

        // ===== Xuất ra byte[] =====
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return bos.toByteArray();
    }




}