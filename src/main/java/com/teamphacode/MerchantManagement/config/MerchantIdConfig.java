package com.teamphacode.MerchantManagement.config;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MerchantIdConfig {

    private static final String PREFIX = "MC";
    private static final int ID_LENGTH = 15;

    public String generateMerchantId() {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        String suffix = uuid.substring(0, ID_LENGTH - PREFIX.length()); // 13 ký tự
        return PREFIX + suffix;
    }
}
