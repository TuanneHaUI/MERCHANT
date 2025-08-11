package com.teamphacode.MerchantManagement.service;

public interface EmailService {

    void sendOtpEmail(String to, String otp);
    void sendOtpMessage(String email, String otp);
}
