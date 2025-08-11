package com.teamphacode.MerchantManagement.domain.dto.request;

import com.teamphacode.MerchantManagement.util.constant.StatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
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
    private String fullName;

    @Column(name = "short_name", length = 25, nullable = false)
    @Size(max = 25, message = "Tên viết tắt không được vượt quá 25 ký tự")
    @Pattern(regexp = "^[A-Z0-9\\s]+$", message = "Tên viết tắt phải là chữ hoa, không có dấu")
    private String shortName;

    @Column(name = "mcc", length = 4, nullable = false)
    @Size(min = 4, max = 4, message = "Mã loại dịch vụ phải có đúng 4 ký tự")
    private String mcc;


    @Column(name = "city", length = 15, nullable = false)
    @Size(max = 15, message = "Thành phố không được vượt quá 15 ký tự")
    private String city;

    @Column(name = "location", length = 15, nullable = false)
    @Size(max = 15, message = "Địa chỉ không được vượt quá 15 ký tự")
    private String location;

    @Column(name = "phone_no", length = 20)
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phoneNo;

    @Column(name = "email", length = 100)
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Column(name = "open_date")
    private LocalDateTime openDate;

    @Column(name = "close_date")
    private LocalDateTime closeDate;

    @NotNull(message = "Lí do không được để trống")
    String reason;


    @Enumerated(EnumType.STRING)
    private StatusEnum status;

}
