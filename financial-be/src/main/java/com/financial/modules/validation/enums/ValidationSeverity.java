package com.financial.modules.validation.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Validation rule severity level")
public enum ValidationSeverity {

    @Schema(description = "Informational observation")
    INFO("Thông tin"),

    @Schema(description = "Low severity notice")
    LOW("Thấp"),

    @Schema(description = "Medium severity warning requiring attention")
    MEDIUM("Trung bình"),

    @Schema(description = "High severity issue requiring review")
    HIGH("Cao"),

    @Schema(description = "Critical accounting discrepancy (e.g. balance sheet equation mismatch)")
    CRITICAL("Nghiêm trọng");

    private final String displayName;

    ValidationSeverity(String displayName) {
        this.displayName = displayName;
    }
}

