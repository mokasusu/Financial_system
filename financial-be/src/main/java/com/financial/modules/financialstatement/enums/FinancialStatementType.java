package com.financial.modules.financialstatement.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Financial statement report type")
public enum FinancialStatementType {
    BALANCE_SHEET("Bảng cân đối kế toán"),
    INCOME_STATEMENT("Báo cáo kết quả hoạt động kinh doanh"),
    CASH_FLOW("Báo cáo lưu chuyển tiền tệ");

    private final String displayName;

    FinancialStatementType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

