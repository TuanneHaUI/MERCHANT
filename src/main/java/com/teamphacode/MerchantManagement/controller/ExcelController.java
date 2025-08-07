package com.teamphacode.MerchantManagement.controller;

import com.teamphacode.MerchantManagement.domain.Merchant;
import com.teamphacode.MerchantManagement.domain.dto.request.MerchantCreateRequest;
import com.teamphacode.MerchantManagement.service.MerchantService;
import com.teamphacode.MerchantManagement.util.excel.BaseExport;
import com.teamphacode.MerchantManagement.util.excel.BaseImport;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@Slf4j
@RequestMapping("/api/v1")
public class ExcelController {

    @Autowired
    private MerchantService merchantService;

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
        response.setHeader("Content-Disposition", "attachment; filename=merchant_import_template.xlsx");

        String[] headers = {
                "Số tài khoản (*)", "Tên đầy đủ (*)", "Tên viết tắt (*)", "MCC (*)", "Thành phố (*)",
                "Địa chỉ (*)", "Số điện thoại", "Email", "Trạng thái (*)", "Mã chi nhánh",
                "Người tạo"
        };

        new BaseExport<>(new ArrayList<>())
                .writeHeaderLine(headers)
                .export(response);
    }

    @PostMapping("/merchant/import/upload")
    public ResponseEntity<?> importMerchantsFromExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vui lòng chọn một file để tải lên.");
        }

        try {
            // **QUAN TRỌNG: Đây là nơi bạn định nghĩa ánh xạ**
            // Key: Tên tiêu đề cột trong file Excel mẫu
            // Value: Tên trường (field) trong class MerchantCreateRequest
            Map<String, String> headerToFieldMap = new HashMap<>();
            headerToFieldMap.put("Số tài khoản (*)", "accountNo");
            headerToFieldMap.put("Tên đầy đủ (*)", "fullName");
            headerToFieldMap.put("Tên viết tắt (*)", "shortName");
            headerToFieldMap.put("MCC (*)", "mcc");
            headerToFieldMap.put("Thành phố (*)", "city");
            headerToFieldMap.put("Địa chỉ (*)", "location");
            headerToFieldMap.put("Số điện thoại", "phoneNo");
            headerToFieldMap.put("Email", "email");
            headerToFieldMap.put("Trạng thái (*)", "status");
            headerToFieldMap.put("Mã chi nhánh", "branchCode");
            headerToFieldMap.put("Người tạo", "createdBy");
            headerToFieldMap.put("Ngày mở (yyyy-MM-dd)", "openDate");

            // Gọi phương thức đọc Excel chung
            List<MerchantCreateRequest> requests = BaseImport.readExcel(
                    file.getInputStream(),
                    MerchantCreateRequest.class,
                    headerToFieldMap
            );

            // Gọi service để xử lý logic nghiệp vụ (validate, lưu CSDL)
            // (Bạn cần có phương thức này trong Service)
            merchantService.handleCreateMultipleMerchants(requests);

            String message = "Tải lên và xử lý thành công " + requests.size() + " merchants từ file: " + file.getOriginalFilename();
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("Lỗi khi import file Excel: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi xảy ra khi xử lý file: " + e.getMessage());
        }
    }
}