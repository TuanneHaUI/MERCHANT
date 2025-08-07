package com.teamphacode.MerchantManagement.util.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * LogUtil đã được chuyển thành một Spring Bean để phá vỡ vòng lặp khởi tạo.
 * Các phương thức không còn là static nữa.
 */
@Component
public class LogUtil {

    private final Logger logger = LoggerFactory.getLogger(LogUtil.class);
    private final ThreadLocal<Integer> stepCounter = ThreadLocal.withInitial(() -> 1);

    /**
     * Khởi tạo hoặc reset bộ đếm step về 1 cho một chuỗi hành động mới.
     */
    public void init() {
        stepCounter.set(1);
    }

    /**
     * Ghi log cho một bước cụ thể và tự động tăng bộ đếm.
     * @param message Nội dung log, có thể chứa placeholder {}
     * @param args Các đối số sẽ được chèn vào placeholder
     */
    public void step(String message, Object... args) {
        int currentStep = stepCounter.get();
        MDC.put("step", String.format("[ %2d ]", currentStep));
        logger.info(message, args);
        stepCounter.set(currentStep + 1);
    }

    /**
     * Dọn dẹp ThreadLocal sau khi hành động kết thúc để tránh memory leak.
     */
    public void cleanup() {
        stepCounter.remove();
    }
}