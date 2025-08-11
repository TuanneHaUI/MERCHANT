package com.teamphacode.MerchantManagement.service.impl;

import com.teamphacode.MerchantManagement.config.RabbitMQConfig;
import com.teamphacode.MerchantManagement.domain.dto.request.OtpMessage;
import com.teamphacode.MerchantManagement.service.EmailService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    private static final Logger logger = LoggerFactory.getLogger(MerchantHistoryServiceImpl.class);
    @Override
    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(new InternetAddress("tuighetlaptrinh@gmail.com", "Merchant System"));
            helper.setSubject("Mã xác thực OTP");

            String html = "<p>Xin chào,</p>" +
                    "<p>Mã OTP của bạn là: <b>" + otp + "</b></p>" +
                    "<p>OTP sẽ hết hạn sau 5 phút.</p>";
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage());
        }
    }

    @Override
    public void sendOtpMessage(String email, String otp) {
        OtpMessage message = new OtpMessage(email, otp);
        logger.info("vô sendOtpMessage");
        rabbitTemplate.convertAndSend(RabbitMQConfig.OTP_EXCHANGE, RabbitMQConfig.OTP_ROUTING_KEY, message);
    }
}
