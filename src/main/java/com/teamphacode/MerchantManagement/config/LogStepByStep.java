package com.teamphacode.MerchantManagement.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // Annotation này dùng cho phương thức
@Retention(RetentionPolicy.RUNTIME) // Tồn tại lúc runtime để AOP có thể đọc được
public @interface LogStepByStep {
    String tag() default ""; // Cho phép đặt tên tag riêng cho hành động
}