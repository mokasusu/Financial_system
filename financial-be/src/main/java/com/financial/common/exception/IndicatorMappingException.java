package com.financial.common.exception;

import lombok.Getter;

@Getter
public class IndicatorMappingException extends RuntimeException {

    public static final String ERROR_CODE = "INDICATOR_MAPPING_FAILED";
    private final String errorCode;

    public IndicatorMappingException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public IndicatorMappingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ERROR_CODE;
    }
}

