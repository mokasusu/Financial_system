package com.financial.modules.riskanalysis.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Financial risk category")
public enum RiskCategory {
    @Schema(description = "Rủi ro thanh khoản (Liquidity Risk)")
    LIQUIDITY("Rủi ro thanh khoản"),

    @Schema(description = "Rủi ro đòn bẩy tài chính và khả năng thanh nợ (Solvency Risk)")
    SOLVENCY("Rủi ro cơ cấu nợ và đòn bẩy"),

    @Schema(description = "Rủi ro suy giảm hiệu quả sinh lời (Profitability Risk)")
    PROFITABILITY("Rủi ro khả năng sinh lời"),

    @Schema(description = "Rủi ro hiệu quả hoạt động và luân chuyển vốn (Operational Efficiency Risk)")
    OPERATIONAL_EFFICIENCY("Rủi ro hiệu quả hoạt động"),

    @Schema(description = "Rủi ro chất lượng dữ liệu và tính nhất quán kế toán (Data Quality Risk)")
    DATA_QUALITY("Rủi ro chất lượng dữ liệu kế toán");

    private final String displayName;

    RiskCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

