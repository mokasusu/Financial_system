package com.financial.common.exception;

import lombok.Getter;

@Getter
public class UnsupportedAccountingStandardException extends RuntimeException {

    public static final String ERROR_CODE = "UNSUPPORTED_ACCOUNTING_STANDARD";
    private final String errorCode;

    public UnsupportedAccountingStandardException(String standard) {
        super("Unsupported accounting standard: " + standard + ". Supported standards: TT200, TT133, TT99.");
        this.errorCode = ERROR_CODE;
    }
}

