package com.teamphacode.MerchantManagement.service.impl;

import com.teamphacode.MerchantManagement.config.MerchantIdConfig;
import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.MerchantHistory;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.request.ReqUpdateMerchant;
import com.teamphacode.MerchantManagement.domain.dto.response.*;
import com.teamphacode.MerchantManagement.domain.dto.specification.MerchantSpecification;
import com.teamphacode.MerchantManagement.mapper.MerchantMapper;
import com.teamphacode.MerchantManagement.repository.MccRepository;
import com.teamphacode.MerchantManagement.repository.MerchantHistoryRepository;
import com.teamphacode.MerchantManagement.repository.MerchantRepository;
import com.teamphacode.MerchantManagement.service.MerchantService;
import com.teamphacode.MerchantManagement.util.LogUtil;
import com.teamphacode.MerchantManagement.util.SecurityUtil;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import com.teamphacode.MerchantManagement.util.errors.AppException;
import com.teamphacode.MerchantManagement.util.errors.IdInvalidException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.teamphacode.MerchantManagement.util.form.ExcelTemplateHelper;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.*;
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
    private MccRepository mccRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private static final String HASH_KEY = "active_merchants";
    @Autowired
    private MerchantHistoryRepository merchantHistoryRepository;
    private static final Logger logger = LoggerFactory.getLogger(MerchantHistoryServiceImpl.class);
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private Validator validator;
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
    public List<Merchant> getAll() {
        return merchantRepository.findAll();
    }

    @Override
    @Transactional
    public void handleCreateMultipleMerchants(List<MerchantCreateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        // --- BƯỚC 1: KIỂM TRA TRÙNG LẶP TRONG FILE ---
        Set<String> accountNosInFile = new HashSet<>();
        Set<String> merchantIdsInFile = new HashSet<>();
        Map<String, List<Integer>> duplicateAccountNosInFile = new HashMap<>();
        Map<String, List<Integer>> duplicateMerchantIdsInFile = new HashMap<>();

        for (int i = 0; i < requests.size(); i++) {
            String accountNo = requests.get(i).getAccountNo();
            String merchantId = requests.get(i).getMerchantId();
            int rowNum = i + 2;

            if (accountNo != null && !accountNo.isEmpty() && !accountNosInFile.add(accountNo)) {
                duplicateAccountNosInFile.computeIfAbsent(accountNo, k -> new ArrayList<>()).add(rowNum);
            }
            if (merchantId != null && !merchantId.isEmpty() && !merchantIdsInFile.add(merchantId)) {
                duplicateMerchantIdsInFile.computeIfAbsent(merchantId, k -> new ArrayList<>()).add(rowNum);
            }
        }

        StringBuilder fileErrors = new StringBuilder();
        duplicateAccountNosInFile.forEach((acc, rows) -> fileErrors.append("Số tài khoản '").append(acc).append("' bị trùng ở các dòng: ").append(rows).append(". "));
        duplicateMerchantIdsInFile.forEach((mid, rows) -> fileErrors.append("Mã định danh '").append(mid).append("' bị trùng ở các dòng: ").append(rows).append(". "));

        if (fileErrors.length() > 0) {
            throw new AppException("Phát hiện dữ liệu trùng lặp trong file: " + fileErrors.toString(), 400);
        }

        // --- BƯỚC 2: KIỂM TRA TRÙNG LẶP HÀNG LOẠT VỚI CSDL ---
        Set<String> existingAccountNos = merchantRepository.findExistingAccountNos(accountNosInFile);
        Set<String> existingMerchantIds = merchantRepository.findExistingMerchantIds(merchantIdsInFile);

        // --- BƯỚC 3: VALIDATE CHI TIẾT VÀ TỔNG HỢP LỖI ---
        List<Merchant> merchantsToSave = new ArrayList<>();
        StringBuilder allErrors = new StringBuilder();

        for (int i = 0; i < requests.size(); i++) {
            MerchantCreateRequest request = requests.get(i);
            int rowNum = i + 2;
            StringBuilder lineErrors = new StringBuilder(); // Dùng để thu thập lỗi của riêng dòng này

            // a. Kiểm tra lỗi validation bean
            Set<ConstraintViolation<MerchantCreateRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                violations.forEach(v -> lineErrors.append(v.getPropertyPath()).append(": ").append(v.getMessage()).append("; "));
            }

            // b. Kiểm tra lỗi trùng lặp với CSDL
            if (request.getAccountNo() != null && existingAccountNos.contains(request.getAccountNo())) {
                lineErrors.append("Số tài khoản '").append(request.getAccountNo()).append("' đã tồn tại; ");
            }
            if (request.getMerchantId() != null && existingMerchantIds.contains(request.getMerchantId())) {
                lineErrors.append("Mã định danh '").append(request.getMerchantId()).append("' đã tồn tại; ");
            }

            // c. Tổng hợp kết quả của dòng này
            if (lineErrors.length() > 0) {
                // Nếu có lỗi, thêm vào danh sách lỗi chung
                allErrors.append("Dòng ").append(rowNum).append(": ").append(lineErrors);
            } else {
                // Nếu không có lỗi, thêm vào danh sách để lưu
                merchantsToSave.add(merchantMapper.toMerchant(request));
            }
        }

        // --- BƯỚC 4: RA QUYẾT ĐỊNH CUỐI CÙNG ---
        if (allErrors.length() > 0) {
            // Nếu có BẤT KỲ lỗi nào được ghi nhận, ném ra exception với tất cả các lỗi
            throw new AppException("Dữ liệu trong file không hợp lệ: " + allErrors.toString(), 400);
        }

        // Chỉ lưu nếu danh sách không rỗng và không có lỗi nào xảy ra
        if (!merchantsToSave.isEmpty()) {
            merchantRepository.saveAll(merchantsToSave);
        }
        redisTemplate.delete(HASH_KEY);
    }

    @Override
    public boolean isChanged(Object oldVal, Object newVal) {
        if (newVal == null) return false;
        return !newVal.equals(oldVal);
    }


    @Override
    @Transactional
    public Merchant handleUpdateMerchant(ReqUpdateMerchant reqUpdateMerchant) throws IdInvalidException {
        logger.info("⏳ Bắt đầu xử lý request: [{} {}]",
                request.getMethod(),
                request.getRequestURI());

        Merchant merchant = merchantRepository.findByAccountNo(reqUpdateMerchant.getAccountNo())
                .orElseThrow(() -> {
                    logger.error("❌ Không tìm thấy merchant với số tài khoản: {}", reqUpdateMerchant.getAccountNo());
                    return new RuntimeException("Không tìm thấy merchant với số tài khoản: " + reqUpdateMerchant.getAccountNo());
                });
        if (reqUpdateMerchant.getStatus() == StatusEnum.Close && reqUpdateMerchant.getCloseDate() == null) {
            logger.warn("⚠\uFE0F Khi cập nhật trạng thái sang ĐÓNG, phải nhập thời điểm kết thúc hoạt động (closeDate).");
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
                logger.info("\uD83D\uDDD1\uFE0F Xóa key trong redis thành công!");
                redisTemplate.delete("MerchantHistoryServiceImpl");
            }
            logger.info("✅ Lưu thành công!");
            merchantHistoryRepository.save(history);
        }

        return merchantRepository.save(merchant);
    }


    @Override
    public ResultPaginationDTO handleReportMerchantByStatus(StatusEnum statusEnum, Pageable pageable)
            throws IdInvalidException {
        logger.info("request: {} {}", request.getMethod(), request.getRequestURI());

        String key = "report_by_status_" + statusEnum.name() + "_page_" + pageable.getPageNumber() +
                "_size_" + pageable.getPageSize() + "_sort_" + pageable.getSort().toString();

        List<Merchant> cacheMerchants = (List<Merchant>) redisTemplate.opsForHash().get(HASH_KEY, key);
        Page<Merchant> merchantPage;

        if (cacheMerchants != null) {
            merchantPage = new PageImpl<>(cacheMerchants, pageable, cacheMerchants.size());
        } else {
            merchantPage = merchantRepository.findByStatus(statusEnum, pageable);
            if (merchantPage.isEmpty()) {
                logger.error("không có dữ liệu của merchant với trạng thái " + statusEnum);
                throw new IdInvalidException("không có dữ liệu của merchant với trạng thái " + statusEnum);
            }
            redisTemplate.opsForHash().put(HASH_KEY, key, merchantPage.getContent());
            redisTemplate.expire(HASH_KEY, Duration.ofHours(1));
        }
        LogUtil.logJsonResponseService(logger, merchantPage, "Page<Merchant> merchantPage: ");

        // Tạo ResultPaginationDTO
        ResultPaginationDTO dto = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(merchantPage.getTotalPages());
        meta.setTotal(merchantPage.getTotalElements());

        dto.setMeta(meta);
        dto.setResult(merchantPage.getContent());

        logger.info("response: " + dto);
        return dto;
    }

    //count merchant by year
    @Override
    public List<ResMerchantYearStatusDTO> handleCountMerchantByYear(int year) {
        logger.info("request: {} {}", request.getMethod(), request.getRequestURI());

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

    // search merchant
    @Override
    public ResultPaginationDTO handleFindByMerchantIdAndAccountNoAndStatus(String merchantId, String accountNo, StatusEnum status, Pageable pageable) {

        logger.info("request: {} {}", request.getMethod(), request.getRequestURI());

        Specification<Merchant> spec = MerchantSpecification.filter(merchantId, accountNo, status);
        Page<Merchant> merchantPage = this.merchantRepository.findAll(spec, pageable);

        LogUtil.logJsonResponseService(logger, merchantPage,"Page<Merchant> merchantPage");

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

    // count transaction by merchant
    @Override
    public List<MerchantTransactionSummaryDTO> handleCountTransactionByMerchant(LocalDateTime fromDate, LocalDateTime toDate) {
        logger.info("request: {} {}", request.getMethod(), request.getRequestURI());
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

    // find transaction detail by merchant
    @Override
    public List<TransactionReportDTO> handleFindTransactionsByMerchant(String merchantId, LocalDateTime fromDate, LocalDateTime toDate) throws IdInvalidException{

        logger.info("request: {} {}", request.getMethod(), request.getRequestURI());

        List<Object[]> result = this.merchantRepository.findTransactionsByMerchant(merchantId, fromDate, toDate);

        if(result.isEmpty()){
            logger.error("không có dữ liệu của transaction với id " +  merchantId);
            throw new IdInvalidException("không có dữ liệu của transaction với id " + merchantId);
        }

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

    //export count merchant by year
    @Override
    public byte[] handleExportMerchantByYear(int year, List<ResMerchantYearStatusDTO> data) throws IOException {
        logger.info("request: {} {}", request.getMethod(), request.getRequestURI());

        Workbook workbook = ExcelTemplateHelper.createWorkbook();
        Sheet sheet = workbook.createSheet("Merchant Year Report");

        // ===== 1. Tiêu đề =====
        ExcelTemplateHelper.addTitle(sheet, "BÁO CÁO TỔNG HỢP DỮ LIỆU", 13);

        // ===== 2. Thông tin Năm và Ngày lấy báo cáo =====
        ExcelTemplateHelper.addInfoRow(sheet, "Năm:", String.valueOf(year), 2);
        ExcelTemplateHelper.addInfoRow(sheet, "Ngày lấy báo cáo:", LocalDate.now().toString(), 3);

        // ===== 3. Header =====
        Row subHeader = sheet.createRow(5);
        String[] header = {"STT", "LOẠI MERCHANT", "THÁNG 01", "THÁNG 02", "THÁNG 03", "THÁNG 04", "THÁNG 05",
                "THÁNG 06", "THÁNG 07", "THÁNG 08", "THÁNG 09", "THÁNG 10", "THÁNG 11", "THÁNG 12"};

        CellStyle headerStyle = ExcelTemplateHelper.createHeaderStyle(workbook);
        for (int i = 0; i < header.length; i++) {
            Cell cell = subHeader.createCell(i);
            cell.setCellValue(header[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }

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
        int startDataRow = 7;
        int totalRowIndex = 6 + rows.size();
        addTotalRow(sheet, "Tổng cộng:", 0, 1, 2, 13, startDataRow, totalRowIndex);

        // ===== 6. Xuất ra byte[] =====
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    //export transaction summary
    @Override
    public byte[] handleExportTransactionSummary(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            List<MerchantTransactionSummaryDTO> data
    ) throws IOException {
        logger.info("request: {} {}", request.getMethod(), request.getRequestURI());

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
        List<Integer> singleColumns = List.of(0); // Cột STT cần merge dọc 2 dòng
        ExcelTemplateHelper.createGroupHeader(sheet, workbook, 6, headers, 2, singleColumns);

        // Header con (sub header)
        Row subHeader = sheet.createRow(7);
        String[] header = {"STT", "SỐ TK", "MÃ MERCHANT", "TÊN TẮT", "THÀNH CÔNG", "THẤT BẠI", "TIMEOUT", "TỔNG"};

        CellStyle headerStyle = ExcelTemplateHelper.createHeaderStyle(workbook);
        for (int i = 0; i < header.length; i++) {
            Cell cell = subHeader.createCell(i);
            cell.setCellValue(header[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
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

    //export transaction detail by merchant
    @Override
    public byte[] handleExportTransactionDetailByMerchant(LocalDateTime fromDate, LocalDateTime toDate, List<TransactionReportDTO> data) throws IOException {
        logger.info("request: {} {}", request.getMethod(), request.getRequestURI());

        Workbook workbook = ExcelTemplateHelper.createWorkbook();
        Sheet sheet = workbook.createSheet("Transaction Detail");

        // ===== Tiêu đề =====
        ExcelTemplateHelper.addTitle(sheet, "BÁO CÁO CHI TIẾT GIAO DỊCH THEO MERCHANT", 8);

        // ===== Thông tin ngày =====
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        ExcelTemplateHelper.addInfoRow(sheet, "Từ ngày:", df.format(fromDate), 2);
        ExcelTemplateHelper.addInfoRow(sheet, "Đến ngày:", df.format(toDate), 3);
        ExcelTemplateHelper.addInfoRow(sheet, "Ngày lấy báo cáo:", df.format(LocalDateTime.now()), 4);


        // ===== Header =====
        Row subHeader = sheet.createRow(6);
        String[] header = {"STT", "CORE REF", "TRANTS REF\nDE#63", "TRACE NO\nDE#11", "TRANS DT\nDE#15", "STATUS\nDE#39", "SENDER ACCT\nDE#102", "SENDER BANK\nDE#32", "RECEIVE ACCT\nDE#103"};

        CellStyle headerStyle = ExcelTemplateHelper.createHeaderStyle(workbook);
        headerStyle.setWrapText(true);

        for (int i = 0; i < header.length; i++) {
            Cell cell = subHeader.createCell(i);
            cell.setCellValue(header[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }

        // ===== Dữ liệu =====
        List<List<Object>> rows = new ArrayList<>();
        int partitionSize = 1000;
        int totalSize = data.size();
        int stt = 1;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // định dạng ngày giờ cho dễ đọc

        for (TransactionReportDTO dto : data) {
            rows.add(List.of(
                    stt++,
                    dto.getCoreRef(),
                    dto.getTransactionRef(),
                    dto.getTraceNo(),
                    dto.getTransactionDate() != null ? dto.getTransactionDate().format(dtf) : "",
                    dto.getStatus(),
                    dto.getSenderAccount(),
                    dto.getSenderBank(),
                    dto.getReceiverAccount()
            ));
        }
        ExcelTemplateHelper.addDataRows(sheet, rows, 7);

        // ===== Xuất ra byte[] =====
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return bos.toByteArray();
    }

    @Override
    public ResultPaginationDTO handleFetchMerchants(Pageable pageable) {
        Page<Merchant> merchantPage = this.merchantRepository.findAll(pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber()+ 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(merchantPage.getTotalPages());
        mt.setTotal(merchantPage.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(merchantPage.getContent());
        return rs;
    }


}