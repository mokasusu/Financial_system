package com.financial.modules.financialindicator.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status of the accounting indicator mapping")
public enum MappingStatus {
    @Schema(description = "Mapping is active and eligible for OCR resolution")
    ACTIVE,

    @Schema(description = "Mapping is deactivated")
    INACTIVE
}

