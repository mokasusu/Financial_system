package com.financial.modules.riskanalysis.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of an identified financial risk")
public enum RiskStatus {
    @Schema(description = "Rủi ro được phát hiện trong lượt phân tích hiện tại")
    DETECTED("Phát hiện"),

    @Schema(description = "Rủi ro đang được theo dõi qua các kỳ phân tích")
    MONITORING("Đang theo dõi"),

    @Schema(description = "Rủi ro đã được giải tỏa hoặc xử lý (chỉ số đã cải thiện vượt ngưỡng rủi ro)")
    RESOLVED("Đã xử lý"),

    @Schema(description = "Chưa có quy tắc rủi ro được cấu hình cho chỉ số này")
    NOT_CONFIGURED("Chưa cấu hình"),

    @Schema(description = "Không đủ dữ liệu tính toán để đánh giá rủi ro")
    INSUFFICIENT_DATA("Thiếu dữ liệu");

    private final String displayName;

    RiskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

