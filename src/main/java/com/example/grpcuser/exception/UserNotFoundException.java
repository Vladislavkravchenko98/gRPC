package com.example.grpcuser.exception;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String message) {
        super(ApiErrorCode.USER_NOT_FOUND, message);
    }
}
