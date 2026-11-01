package com.financial.common.exception;

import lombok.Getter;

@Getter
public class AssessmentNotFoundException extends RuntimeException {

    public static final String ERROR_CODE = "ASSESSMENT_NOT_FOUND";
    private final String errorCode;

    public AssessmentNotFoundException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public AssessmentNotFoundException(Long id) {
        super("Financial assessment not found with ID: " + id);
        this.errorCode = ERROR_CODE;
    }
}

