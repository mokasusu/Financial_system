package com.financial.common.exception;

import lombok.Getter;

@Getter
public class DuplicateCriteriaException extends RuntimeException {

    public static final String ERROR_CODE = "DUPLICATE_ACTIVE_CRITERIA";
    private final String errorCode;

    public DuplicateCriteriaException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public static DuplicateCriteriaException forRatio(String ratioCode) {
        return new DuplicateCriteriaException("An active evaluation criterion already exists for ratio: " + ratioCode);
    }
}

