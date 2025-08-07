package com.teamphacode.MerchantManagement.util.errors;

public class AppException extends RuntimeException {
    private final int errorCode;

    public AppException(String message) {
        super(message);
        this.errorCode = 400;
    }

    public AppException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}

