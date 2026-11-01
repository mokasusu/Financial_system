package com.financial.modules.assessment.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Overall status of the financial assessment execution")
public enum AssessmentStatus {
    @Schema(description = "Assessment executed successfully across all evaluated indicators")
    COMPLETED,

    @Schema(description = "Assessment completed with partial or missing data components")
    INCOMPLETE,

    @Schema(description = "Assessment failed due to internal error or corrupt statement data")
    FAILED
}

