package com.teamphacode.MerchantManagement.util;

import com.teamphacode.MerchantManagement.config.RabbitMQConfig;
import com.teamphacode.MerchantManagement.domain.dto.request.OtpMessage;
import com.teamphacode.MerchantManagement.service.impl.EmailServiceImpl;
import com.teamphacode.MerchantManagement.service.impl.MerchantHistoryServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OtpConsumer {
    private final EmailServiceImpl emailServiceImpl;

    private static final Logger logger = LoggerFactory.getLogger(MerchantHistoryServiceImpl.class);

    public OtpConsumer(EmailServiceImpl emailServiceImpl) {
        this.emailServiceImpl = emailServiceImpl;
    }


    @RabbitListener(queues = RabbitMQConfig.OTP_QUEUE)
    public void receiveOtpMessage(OtpMessage message) {
        logger.info("đã chạy vô receiveOtpMessage");
        emailServiceImpl.sendOtpEmail(message.getEmail(), message.getOtp());
    }
}
