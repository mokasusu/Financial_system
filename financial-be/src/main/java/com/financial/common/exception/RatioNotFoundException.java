package com.financial.common.exception;

import lombok.Getter;

@Getter
public class RatioNotFoundException extends RuntimeException {

    public static final String ERROR_CODE = "RATIO_NOT_FOUND";
    private final String errorCode;

    public RatioNotFoundException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public static RatioNotFoundException byCode(String code) {
        return new RatioNotFoundException("Financial ratio definition not found with code: " + code);
    }
}

