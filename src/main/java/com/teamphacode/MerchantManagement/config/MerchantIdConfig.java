package com.teamphacode.MerchantManagement.config;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MerchantIdConfig {
    public String generateMerchantId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 15).toUpperCase();
    }
}