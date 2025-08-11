package com.teamphacode.MerchantManagement.util.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;


@Component
public class LogUtil {

    private final Logger logger = LoggerFactory.getLogger(LogUtil.class);
    private final ThreadLocal<Integer> stepCounter = ThreadLocal.withInitial(() -> 1);


    public void init() {
        stepCounter.set(1);
    }


    public void step(String message, Object... args) {
        int currentStep = stepCounter.get();
        MDC.put("step", String.format("[ %2d ]", currentStep));
        logger.info(message, args);
        stepCounter.set(currentStep + 1);
    }


    public void cleanup() {
        stepCounter.remove();
    }
}