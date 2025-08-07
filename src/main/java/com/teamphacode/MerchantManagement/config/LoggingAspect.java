package com.teamphacode.MerchantManagement.config;

import com.teamphacode.MerchantManagement.util.logging.LogUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Autowired
    private ApplicationContext context;

    private volatile LogUtil logUtilInstance;

    /**
     * Lấy bean LogUtil một cách an toàn và "lười biếng" (lazy) để phá vỡ vòng lặp khởi tạo.
     * Chỉ lấy bean từ context ở lần gọi đầu tiên.
     */
    private LogUtil getLogUtil() {
        if (this.logUtilInstance == null) {
            synchronized (this) {
                if (this.logUtilInstance == null) {
                    this.logUtilInstance = context.getBean(LogUtil.class);
                }
            }
        }
        return this.logUtilInstance;
    }

    @Around("@annotation(com.teamphacode.MerchantManagement.config.LogStepByStep)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        LogUtil logUtil = getLogUtil();
        logUtil.init();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        LogStepByStep logAnnotation = signature.getMethod().getAnnotation(LogStepByStep.class);

        String tag = logAnnotation.tag().isEmpty()
                ? joinPoint.getTarget().getClass().getSimpleName()
                : logAnnotation.tag();
        MDC.put("tag", String.format("[%s]", tag));

        logUtil.step("Bắt đầu: {}", signature.getName());

        Object result;
        try {
            result = joinPoint.proceed();
        } finally {
            logUtil.step("Kết thúc: {}", signature.getName());
            MDC.remove("tag");
            MDC.remove("step");
            logUtil.cleanup();
        }

        return result;
    }
}