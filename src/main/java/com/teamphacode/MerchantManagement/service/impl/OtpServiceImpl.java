package com.teamphacode.MerchantManagement.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;


@Service
public class OtpServiceImpl {

    private static final String OTP_PREFIX = "OTP_";
    private final StringRedisTemplate redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(MerchantHistoryServiceImpl.class);
    public OtpServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveOtp(String email, String otp) {
        String key = OTP_PREFIX + email.toLowerCase();
        logger.info("redis saveOtp:"+key);
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(5));
    }

    public boolean verifyOtp(String email, String otp) {
        String key = OTP_PREFIX + email.toLowerCase();
        logger.info("redis verifyOtp:"+key);
        String cachedOtp = redisTemplate.opsForValue().get(key);
        boolean ok = Objects.equals(otp, cachedOtp);
        if (ok) {
            logger.info("Xóa otp trong redis:"+cachedOtp);
            redisTemplate.delete(key);
        }
        return ok;
    }
}
