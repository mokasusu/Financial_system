package com.financial.modules.assessment.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Overall financial health classification level")
public enum HealthClassification {
    @Schema(description = "Sức khỏe tài chính xuất sắc (Excellent)")
    EXCELLENT("Xuất sắc"),

    @Schema(description = "Sức khỏe tài chính tốt (Good)")
    GOOD("Tốt"),

    @Schema(description = "Sức khỏe tài chính trung bình (Average)")
    AVERAGE("Trung bình"),

    @Schema(description = "Sức khỏe tài chính yếu (Weak)")
    WEAK("Yếu"),

    @Schema(description = "Sức khỏe tài chính nguy cấp / Báo động (Critical)")
    CRITICAL("Nguy cấp"),

    @Schema(description = "Chưa cấu hình quy tắc phân loại sức khỏe tài chính")
    NOT_CONFIGURED("Chưa cấu hình");

    private final String displayName;

    HealthClassification(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

