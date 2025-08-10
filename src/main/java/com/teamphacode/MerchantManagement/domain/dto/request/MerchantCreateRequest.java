package com.teamphacode.MerchantManagement.domain.dto.request;
import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantCreateRequest {

    @NotBlank(message = "Mã định danh merchant không được để trống")
    @Size(max = 15, message = "Mã định danh merchant không được vượt quá 15 ký tự")
    @Pattern(regexp = "^MC[A-Z0-9]*$", message = "Mã định danh phải bắt đầu bằng 'MC' và chỉ chứa chữ in hoa, số")
    private String merchantId;

    @NotBlank(message = "Số tài khoản không được để trống")
    @Size(max = 19, message = "Số tài khoản không được vượt quá 19 ký tự")
    private String accountNo;

    @NotBlank(message = "Tên đầy đủ không được để trống")
    @Size(max = 200, message = "Tên đầy đủ không được vượt quá 200 ký tự")
    @Pattern(regexp = "^[A-Z0-9\\s]+$", message = "Tên đầy đủ phải là chữ hoa, không có dấu")
    private String fullName;

    @NotBlank(message = "Tên viết tắt không được để trống")
    @Size(max = 25, message = "Tên viết tắt không được vượt quá 25 ký tự")
    @Pattern(regexp = "^[A-Z0-9\\s]+$", message = "Tên viết tắt phải là chữ hoa, không có dấu")
    private String shortName;

    @NotBlank(message = "Mã loại dịch vụ không được để trống")
    @Size(min = 4, max = 4, message = "Mã loại dịch vụ phải có đúng 4 ký tự")
    private String mcc;

    @NotBlank(message = "Thành phố không được để trống")
    @Size(max = 15, message = "Thành phố không được vượt quá 15 ký tự")
    private String city;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 15, message = "Địa chỉ không được vượt quá 15 ký tự")
    private String location;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phoneNo;

    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotNull(message = "Trạng thái không được để trống")
    private StatusEnum status;

    private LocalDateTime openDate;

    private LocalDateTime closeDate;

    private String branchCode;
}