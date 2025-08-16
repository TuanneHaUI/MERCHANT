package com.teamphacode.MerchantManagement.domain.dto.request;

import com.teamphacode.MerchantManagement.domain.Role;
import com.teamphacode.MerchantManagement.util.constant.GenderEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResupdateUser {

    @NotNull(message = "id không được để trống")
    private Long id;

    @NotBlank(message = "address không được để trống")
    private String address;

    @NotNull(message = "age không được để trống")
    private Integer age;

    @NotBlank(message = "email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "name không được để trống")
    private String name;

    @NotBlank(message = "password không được để trống")
    private String password;

    @NotNull(message = "role không được để trống")
    private Role role;

    @Enumerated(EnumType.STRING)
    private GenderEnum gender;
}
