package com.financial.common.exception;

import lombok.Getter;

@Getter
public class MappingNotFoundException extends RuntimeException {

    public static final String ERROR_CODE = "MAPPING_NOT_FOUND";
    private final String errorCode;

    public MappingNotFoundException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public static MappingNotFoundException forLookup(String standard, String statementType, String codeOrKey) {
        return new MappingNotFoundException(
                String.format("No indicator mapping found for standard '%s', statement '%s', and code/key '%s'",
                        standard, statementType, codeOrKey)
        );
    }
}

