package com.example.grpcuser.exception;

public class DuplicateCarVinException extends BusinessException {

    public DuplicateCarVinException(String message) {
        super(ApiErrorCode.DUPLICATE_CAR_VIN, message);
    }
}
