package com.teamphacode.MerchantManagement.config;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(1) // Đảm bảo filter này chạy trước các filter khác
public class LoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Tạo một traceId duy nhất cho request này
        String traceId = UUID.randomUUID().toString();

        // 2. Đặt traceId vào MDC
        // Bất kỳ log nào được ghi từ đây trở đi TRONG CÙNG THREAD NÀY sẽ có traceId
        MDC.put(TRACE_ID_KEY, traceId);

        try {
            // 3. Chuyển request cho các filter và controller tiếp theo xử lý
            filterChain.doFilter(request, response);
        } finally {
            // 4. RẤT QUAN TRỌNG: Xóa traceId khỏi MDC sau khi request hoàn tất
            // Vì các web server sử dụng lại thread (thread pool), nếu không xóa,
            // traceId của request này có thể bị "rò rỉ" sang request khác.
            MDC.remove(TRACE_ID_KEY);
        }
    }
}