package com.financial.modules.calculationengine.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Calculation execution status for financial ratios")
public enum CalculationStatus {

    @Schema(description = "Calculation succeeded with a valid numeric result")
    SUCCESS("Thành công"),

    @Schema(description = "Partially completed calculation")
    PARTIAL("Thành công một phần"),

    @Schema(description = "Insufficient or missing data to evaluate formula")
    INSUFFICIENT_DATA("Thiếu dữ liệu tính toán"),

    @Schema(description = "Mathematical division by zero encountered")
    DIVISION_BY_ZERO("Lỗi chia cho 0"),

    @Schema(description = "Input value is invalid or corrupted")
    INVALID_INPUT("Dữ liệu đầu vào không hợp lệ"),

    @Schema(description = "Ratio formula or input dependencies not configured")
    NOT_CONFIGURED("Chưa cấu hình công thức hoặc chỉ tiêu"),

    @Schema(description = "Calculation failed due to an unexpected error")
    FAILED("Tính toán thất bại");

    private final String displayName;

    CalculationStatus(String displayName) {
        this.displayName = displayName;
    }
}

