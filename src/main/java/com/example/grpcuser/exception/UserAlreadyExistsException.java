package com.example.grpcuser.exception;

public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String message) {
        super(ApiErrorCode.USER_ALREADY_EXISTS, message);
    }
}
