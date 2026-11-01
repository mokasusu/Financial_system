package com.financial.modules.financialstatement.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Indicator mapping status against standard financial taxonomy")
public enum MappingStatus {
    MATCHED("Khớp chuẩn"),
    NEEDS_REVIEW("Cần rà soát"),
    UNMAPPED("Chưa chuẩn hóa");

    private final String displayName;

    MappingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

