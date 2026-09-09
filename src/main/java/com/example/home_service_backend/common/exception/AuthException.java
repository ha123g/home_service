package com.example.home_service_backend.common.exception;

public class AuthException extends Exception {
    private ExceptionCode errorCode;
    public AuthException(ExceptionCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    public ExceptionCode getErrorCode() {
        return errorCode;
    }
}
