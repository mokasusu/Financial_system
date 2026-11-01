package com.financial.modules.financialindicator.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category of financial ratio")
public enum FinancialRatioCategory {
    @Schema(description = "Liquidity ratios (Khả năng thanh toán)")
    LIQUIDITY,

    @Schema(description = "Solvency ratios (Cơ cấu vốn / Đòn bẩy tài chính)")
    SOLVENCY,

    @Schema(description = "Profitability ratios (Khả năng sinh lời)")
    PROFITABILITY,

    @Schema(description = "Efficiency / Activity ratios (Hiệu quả hoạt động)")
    EFFICIENCY
}

