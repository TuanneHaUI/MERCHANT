package com.teamphacode.MerchantManagement.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.teamphacode.MerchantManagement.service.impl.MerchantHistoryServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class LogUtil {

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT); // pretty print

    public static void logJsonResponse(Logger logger, HttpStatus status, Object body) {
        try {
            String jsonBody = mapper.writeValueAsString(body);
            logger.info("response status: {}, body:\n{}", status.value(), jsonBody);
        } catch (Exception e) {
            logger.error("Error serializing response", e);
        }
    }
}
