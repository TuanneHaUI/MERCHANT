package com.teamphacode.MerchantManagement.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String OTP_QUEUE = "otp-queue";
    public static final String OTP_EXCHANGE = "otp-exchange";
    public static final String OTP_ROUTING_KEY = "otp.routing.key";

    // Định nghĩa Queue (hàng đợi) để chứa message OTP
    @Bean
    public Queue otpQueue() {
        return QueueBuilder.durable(OTP_QUEUE).build();
    }


    // Định nghĩa Exchange dạng Direct (gửi message theo routing key chính xác)
    @Bean
    public DirectExchange otpExchange() {
        return new DirectExchange(OTP_EXCHANGE);
    }

    // Binding: kết nối Queue với Exchange bằng Routing Key
    @Bean
    public Binding otpBinding(Queue otpQueue, DirectExchange otpExchange) {
        return BindingBuilder.bind(otpQueue).to(otpExchange).with(OTP_ROUTING_KEY);
    }


    // Cấu hình RabbitTemplate để gửi message (Producer sử dụng)
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }

    // Converter chuyển message thành JSON và ngược lại
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    // Cấu hình Consumer (RabbitListener) qua ContainerFactory
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter());

        // Số lượng consumer chạy song song
        factory.setConcurrentConsumers(3);        // Tối thiểu 3 consumer
        factory.setMaxConcurrentConsumers(10);    // Tối đa 10 consumer
        factory.setPrefetchCount(10);             // Mỗi consumer lấy trước 10 message để xử lý

        return factory;
    }
}
