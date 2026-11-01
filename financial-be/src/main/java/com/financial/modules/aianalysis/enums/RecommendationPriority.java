package com.financial.modules.aianalysis.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Action priority for financial recommendation")
public enum RecommendationPriority {

    @Schema(description = "Low priority")
    LOW("Thấp"),

    @Schema(description = "Medium priority")
    MEDIUM("Trung bình"),

    @Schema(description = "High priority")
    HIGH("Cao"),

    @Schema(description = "Critical priority")
    CRITICAL("Khẩn cấp");

    private final String displayName;

    RecommendationPriority(String displayName) {
        this.displayName = displayName;
    }

    public static RecommendationPriority fromStringSafe(String val) {
        if (val == null || val.isBlank()) {
            return MEDIUM;
        }
        try {
            return RecommendationPriority.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return MEDIUM;
        }
    }
}

