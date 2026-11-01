package com.financial.common.exception;

import lombok.Getter;

@Getter
public class CriteriaNotFoundException extends RuntimeException {

    public static final String ERROR_CODE = "CRITERIA_NOT_FOUND";
    private final String errorCode;

    public CriteriaNotFoundException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public CriteriaNotFoundException(Long id) {
        super("Evaluation criterion not found with ID: " + id);
        this.errorCode = ERROR_CODE;
    }

    public static CriteriaNotFoundException byRatioCode(String ratioCode) {
        return new CriteriaNotFoundException("No evaluation criteria found for ratio code: " + ratioCode);
    }
}

