package com.teamphacode.MerchantManagement.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.teamphacode.MerchantManagement.service.impl.MerchantHistoryServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


public class LogUtil {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // hỗ trợ LocalDateTime, LocalDate, Instant...
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT); // pretty print

    public static void logJsonResponse(Logger logger, HttpStatus status, Object body) {
        try {
            String jsonBody = mapper.writeValueAsString(body);
            logger.info("response status: {}, body:\n{}", status.value(), jsonBody);
        } catch (Exception e) {
            logger.error("Error serializing response", e);
        }
    }

    public static void logJsonResponseService(Logger logger, Object body, String title) {
        try {
            String jsonBody = mapper.writeValueAsString(body);
            logger.info(title+":\n{}",  jsonBody);
        } catch (Exception e) {
            logger.error("Error serializing response", e);
        }
    }
}
