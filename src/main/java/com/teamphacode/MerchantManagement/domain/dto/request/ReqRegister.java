package com.teamphacode.MerchantManagement.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqRegister {
    private String email;
    private String fullName;
    private String passWord;
    private String confirmPassWord;
    private String otp;
}
