package com.teamphacode.MerchantManagement.domain.dto.request;

import com.teamphacode.MerchantManagement.domain.Role;
import com.teamphacode.MerchantManagement.util.constant.GenderEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreatedUser {
    private String email;
    private String name;
    private String password;
    private String address;
    private int age;
    private GenderEnum gender;
    private Role role;
}
