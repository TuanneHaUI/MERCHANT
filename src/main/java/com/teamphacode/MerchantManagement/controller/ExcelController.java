package com.teamphacode.MerchantManagement.controller;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.domain.dto.response.ResImportExcel;
import com.teamphacode.MerchantManagement.domain.dto.response.RestResponse;
import com.teamphacode.MerchantManagement.service.MerchantService;
import com.teamphacode.MerchantManagement.service.impl.MccServiceImpl;
import com.teamphacode.MerchantManagement.service.impl.MerchantHistoryServiceImpl;
import com.teamphacode.MerchantManagement.service.impl.MerchantServiceImpl;
import com.teamphacode.MerchantManagement.util.LogUtil;
import com.teamphacode.MerchantManagement.util.excel.BaseExport;
import com.teamphacode.MerchantManagement.util.excel.BaseImport;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@Slf4j
@RequestMapping("/api/v1")
public class ExcelController {
    @Autowired
    private MerchantService merchantService;
    private static final Logger logger = LoggerFactory.getLogger(MerchantServiceImpl.class);

    @GetMapping("/merchant/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=merchants_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Merchant> listData = merchantService.getAll();

        String[] headers = {
                "Mã Merchant", "Số tài khoản", "Tên đầy đủ", "Tên viết tắt", "MCC",
                "Thành phố", "Địa chỉ", "Số điện thoại", "Email", "Trạng thái",
                "Ngày mở", "Ngày đóng", "Mã chi nhánh", "Người tạo"
        };

        String[] fields = {
                "merchantId", "accountNo", "fullName", "shortName", "mcc",
                "city", "location", "phoneNo", "email", "status",
                "openDate", "closeDate", "branchCode", "createdBy"
        };

        new BaseExport<>(listData)
                .writeHeaderLine(headers)
                .writeDataLines(fields, Merchant.class)
                .export(response);
    }

    @GetMapping("/merchant/export/sample")
    public void exportToSample(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=sample.xlsx");
        ClassPathResource resource = new ClassPathResource("static/sample.xlsx");
        try (InputStream inputStream = resource.getInputStream()) {
            // StreamUtils.copy là một tiện ích của Spring để sao chép stream một cách hiệu quả
            StreamUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        }
    }

    @PostMapping(value = "/merchant/import/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import file Excel merchant")
    public ResponseEntity<?> importMerchantsFromExcel(
            @RequestParam String requestId,
            @RequestParam String requestTime,
            @Parameter(
                    description = "File Excel upload",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestParam("file") MultipartFile file) throws Exception {
        logger.info("requestBody: "+ requestId + " requestTime: " + requestTime + " data: file");
        if (file.isEmpty()) {
            logger.warn("file empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vui lòng chọn một file để tải lên.");
        }

        Map<String, String> headerToFieldMap = new HashMap<>();
        headerToFieldMap.put("MÃ ĐỊNH DANH", "merchantId");
        headerToFieldMap.put("SỐ TK", "accountNo");
        headerToFieldMap.put("TÊN MERCHANT", "fullName");
        headerToFieldMap.put("TÊN TẮT", "shortName");
        headerToFieldMap.put("DỊCH VỤ", "mcc");
        headerToFieldMap.put("THÀNH PHỐ", "city");
        headerToFieldMap.put("ĐỊA CHỈ", "location");
        headerToFieldMap.put("SỐ ĐT", "phoneNo");
        headerToFieldMap.put("EMAIL (NẾU CÓ)", "email");
        headerToFieldMap.put("TRẠNG THÁI", "status");
        headerToFieldMap.put("THỜI ĐIỂM HĐ", "openDate");
        headerToFieldMap.put("THỜI ĐIỂM KTHD", "closeDate");
        headerToFieldMap.put("MÃ CN", "branchCode");

        // Gọi phương thức đọc Excel chung
        List<MerchantCreateRequest> requests = BaseImport.readExcel(
                file.getInputStream(),
                MerchantCreateRequest.class,
                headerToFieldMap
        );


        merchantService.handleCreateMultipleMerchants(requests);
        String message = "Tải lên và xử lý thành công " + requests.size() + " merchants từ file: " + file.getOriginalFilename();
        LogUtil.logJsonResponse(logger, HttpStatus.OK, message);
        return ResponseEntity.ok(new ResImportExcel(message));
    }

    //-----------------------------------------------------------------------------------------------------------
    @GetMapping("/merchant/export/sample2")
    public void exportToSample2(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=merchant_import_template.xlsx");

        String[] headers = {
                "STT", "MÃ ĐỊNH DANH", "SỐ TK", "TÊN MERCHANT", "TÊN TẮT", "DỊCH VỤ", "THÀNH PHỐ", "ĐỊA CHỈ", "SỐ ĐT",
                "EMAIL (NẾU CÓ)", "TRẠNG THÁI", "THỜI ĐIỂM HĐ", "THỜI ĐIỂM KTHD", "MÃ CN"
        };

        new BaseExport<>(new ArrayList<>())
                .writeHeaderLine(headers)
                .export(response);
    }
}