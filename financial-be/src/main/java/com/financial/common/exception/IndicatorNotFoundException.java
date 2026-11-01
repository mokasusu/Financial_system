package com.financial.common.exception;

import lombok.Getter;

@Getter
public class IndicatorNotFoundException extends RuntimeException {

    public static final String ERROR_CODE = "INDICATOR_NOT_FOUND";
    private final String errorCode;

    public IndicatorNotFoundException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public IndicatorNotFoundException(Long id) {
        super("Financial indicator not found with ID: " + id);
        this.errorCode = ERROR_CODE;
    }

    public static IndicatorNotFoundException byCode(String code) {
        return new IndicatorNotFoundException("Financial indicator not found with code: " + code);
    }
}

