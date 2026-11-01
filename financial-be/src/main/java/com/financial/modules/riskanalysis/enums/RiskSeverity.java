package com.financial.modules.riskanalysis.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Financial risk severity classification level")
public enum RiskSeverity {
    @Schema(description = "Mức độ rủi ro thấp (Low)")
    LOW("Thấp"),

    @Schema(description = "Mức độ rủi ro trung bình (Medium)")
    MEDIUM("Trung bình"),

    @Schema(description = "Mức độ rủi ro cao (High)")
    HIGH("Cao"),

    @Schema(description = "Mức độ rủi ro nghiêm trọng / Khẩn cấp (Critical)")
    CRITICAL("Nghiêm trọng");

    private final String displayName;

    RiskSeverity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

