package com.teamphacode.MerchantManagement.domain;


import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import com.teamphacode.MerchantManagement.util.SecurityUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;

@Entity
@Table(name = "merchants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant {

    @Id
    @Column(name = "merchant_id", length = 15)
    @NotBlank(message = "Mã định danh merchant không được để trống")
    @Size(max = 15, message = "Mã định danh merchant không được vượt quá 15 ký tự")
    private String merchantId;

    @Column(name = "account_no", length = 19, nullable = false, unique = true)
    @NotBlank(message = "Số tài khoản không được để trống")
    @Size(max = 19, message = "Số tài khoản không được vượt quá 19 ký tự")
    private String accountNo;

    @Column(name = "full_name", length = 200, nullable = false)
    @NotBlank(message = "Tên đầy đủ không được để trống")
    @Size(max = 200, message = "Tên đầy đủ không được vượt quá 200 ký tự")
    @Pattern(regexp = "^[A-Z0-9\\s]+$", message = "Tên đầy đủ phải là chữ hoa, không có dấu")
    private String fullName;

    @Column(name = "short_name", length = 25, nullable = false)
    @NotBlank(message = "Tên viết tắt không được để trống")
    @Size(max = 25, message = "Tên viết tắt không được vượt quá 25 ký tự")
    @Pattern(regexp = "^[A-Z0-9\\s]+$", message = "Tên viết tắt phải là chữ hoa, không có dấu")
    private String shortName;

    @Column(name = "mcc", length = 4, nullable = false)
    @NotBlank(message = "Mã loại dịch vụ không được để trống")
    @Size(min = 4, max = 4, message = "Mã loại dịch vụ phải có đúng 4 ký tự")
    private String mcc;

    @Column(name = "city", length = 15, nullable = false)
    @NotBlank(message = "Thành phố không được để trống")
    @Size(max = 15, message = "Thành phố không được vượt quá 15 ký tự")
    private String city;

    @Column(name = "location", length = 15, nullable = false)
    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 15, message = "Địa chỉ không được vượt quá 15 ký tự")
    private String location;

    @Column(name = "phone_no", length = 20)
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phoneNo;

    @Column(name = "email", length = 100)
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Column(name = "status", nullable = false)
    @NotNull(message = "Trạng thái không được để trống")
    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    @Column(name = "open_date")
    private LocalDate openDate;

    @Column(name = "close_date")
    private LocalDate closeDate;

    @Column(name = "branch_code", length = 4)
    @Size(max = 4, message = "Mã chi nhánh không được vượt quá 4 ký tự")
    private String branchCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.updatedAt = LocalDateTime.now();
    }


}
