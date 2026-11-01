package com.financial.modules.financialstatement.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Processing lifecycle status of financial statement")
public enum FinancialStatementStatus {
    QUEUED("Đang chờ xử lý"),
    PROCESSING("Đang xử lý OCR"),
    COMPLETED("Xử lý OCR thành công"),
    FAILED("Xử lý thất bại"),
    CANCEL_REQUESTED("Yêu cầu hủy xử lý"),
    CANCELLED("Đã hủy xử lý"),
    REVIEWING_DATA("Đang rà soát dữ liệu"),
    DATA_CONFIRMED("Dữ liệu tài chính đã xác nhận");

    private final String displayName;

    FinancialStatementStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

