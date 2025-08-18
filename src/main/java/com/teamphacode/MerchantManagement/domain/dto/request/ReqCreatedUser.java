package com.teamphacode.MerchantManagement.domain.dto.request;

import com.teamphacode.MerchantManagement.domain.Role;
import com.teamphacode.MerchantManagement.util.constant.GenderEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreatedUser {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Tên không được để trống")
    @Size(min = 2, max = 50, message = "Tên phải từ 2 đến 50 ký tự")
    private String name;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    private String address;

    @Min(value = 5, message = "Tuổi phải >= 5")
    private int age;

    @NotNull(message = "Giới tính không được null")
    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    @NotNull(message = "Role không được null")
    private Role role;

}
