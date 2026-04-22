package com.example.grpcuser.exception;

public abstract class BusinessException extends RuntimeException {

    private final ApiErrorCode errorCode;

    protected BusinessException(ApiErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiErrorCode getErrorCode() {
        return errorCode;
    }
}
