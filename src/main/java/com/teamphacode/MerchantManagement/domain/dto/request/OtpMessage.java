package com.teamphacode.MerchantManagement.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpMessage {
    private String email;
    private String otp;
}
