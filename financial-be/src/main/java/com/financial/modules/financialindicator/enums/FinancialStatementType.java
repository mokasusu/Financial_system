package com.financial.modules.financialindicator.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Financial statement type")
public enum FinancialStatementType {
    @Schema(description = "Balance sheet (Bảng cân đối kế toán)")
    BALANCE_SHEET,

    @Schema(description = "Income statement (Báo cáo kết quả hoạt động kinh doanh)")
    INCOME_STATEMENT,

    @Schema(description = "Cash flow statement (Báo cáo lưu chuyển tiền tệ)")
    CASH_FLOW_STATEMENT
}

