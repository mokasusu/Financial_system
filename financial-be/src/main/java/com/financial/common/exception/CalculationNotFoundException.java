package com.financial.common.exception;

import lombok.Getter;

@Getter
public class CalculationNotFoundException extends RuntimeException {

    public static final String ERROR_CODE = "CALCULATION_NOT_FOUND";
    private final String errorCode;

    public CalculationNotFoundException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public CalculationNotFoundException(Long id) {
        super("Financial calculation not found with ID: " + id);
        this.errorCode = ERROR_CODE;
    }
}

