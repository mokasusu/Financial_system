package com.financial.common.exception;

import lombok.Getter;

@Getter
public class InvalidAssessmentStateException extends RuntimeException {

    public static final String ERROR_CODE = "INVALID_ASSESSMENT_STATE";
    private final String errorCode;

    public InvalidAssessmentStateException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }
}

