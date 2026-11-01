package com.financial.modules.assessment.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Scoring configuration and calculation status")
public enum AssessmentScoringStatus {
    @Schema(description = "Scoring and category weights completely calculated")
    COMPLETED,

    @Schema(description = "Partial scoring weights or incomplete scoring rules configured")
    PARTIAL,

    @Schema(description = "No scoring rules or weights configured in the system")
    NOT_CONFIGURED,

    @Schema(description = "Data is insufficient to compute weighted financial scores")
    INSUFFICIENT_DATA
}

