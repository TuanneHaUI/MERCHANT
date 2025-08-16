package com.teamphacode.MerchantManagement.domain.dto.request;

import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ReqUpdateMerchant {

    @Column(name = "account_no", length = 19, nullable = false, unique = true)
    @NotBlank(message = "Số tài khoản không được để trống")
    @Size(max = 19, message = "Số tài khoản không được vượt quá 19 ký tự")
    private String accountNo;

    @Column(name = "full_name", length = 200, nullable = false)
    @Size(max = 200, message = "Tên đầy đủ không được vượt quá 200 ký tự")
    @Pattern(regexp = "^[A-Z0-9\\s]+$", message = "Tên đầy đủ phải là chữ hoa, không có dấu")
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Column(name = "short_name", length = 25, nullable = false)
    @Size(max = 25, message = "Tên viết tắt không được vượt quá 25 ký tự")
    @Pattern(regexp = "^[A-Z0-9\\s]+$", message = "Tên viết tắt phải là chữ hoa, không có dấu")
    @NotBlank(message = "Tên không được để trống")
    private String shortName;

    @Column(name = "mcc", length = 4, nullable = false)
    @Size(min = 4, max = 4, message = "Mã loại dịch vụ phải có đúng 4 ký tự")
    @NotBlank(message = "Mã loại dịch vụ không được để trống")
    private String mcc;

    @Column(name = "city", length = 50, nullable = false)
    @Size(max = 50, message = "Thành phố không được vượt quá 15 ký tự")
    @NotBlank(message = "Thành phố không được để trống")
    private String city;

    @Column(name = "location", length = 50, nullable = false)
    @Size(max = 50, message = "Địa chỉ không được vượt quá 15 ký tự")
    @NotBlank(message = "Địa chỉ không được để trống")
    private String location;

    @Column(name = "phone_no", length = 40)
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNo;

    @Column(name = "email", length = 100)
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    @Email(message = "Email không đúng định dạng")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @Column(name = "open_date")
    private LocalDateTime openDate;

    @Column(name = "close_date")
    private LocalDateTime closeDate;

    @NotBlank(message = "Lí do không được để trống")
    private String reason;

    @NotNull(message = "Trạng thái không được để trống")
    @Enumerated(EnumType.STRING)
    private StatusEnum status;
}
