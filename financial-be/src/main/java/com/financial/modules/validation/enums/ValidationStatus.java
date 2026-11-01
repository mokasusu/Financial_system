package com.financial.modules.validation.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Validation rule execution status")
public enum ValidationStatus {

    @Schema(description = "Validation rule passed within accepted tolerance")
    PASS("Đạt"),

    @Schema(description = "Potential issue or significant change requiring attention")
    WARNING("Cảnh báo"),

    @Schema(description = "Validation rule failed (e.g. accounting equation balance mismatch)")
    FAIL("Không đạt"),

    @Schema(description = "Rule is not applicable to the given accounting standard or statement type")
    NOT_APPLICABLE("Không áp dụng"),

    @Schema(description = "User review or missing information is required to verify consistency")
    REVIEW_REQUIRED("Cần rà soát"),

    @Schema(description = "Validation could not be executed due to technical or configuration error")
    ERROR("Lỗi xử lý");

    private final String displayName;

    ValidationStatus(String displayName) {
        this.displayName = displayName;
    }
}

