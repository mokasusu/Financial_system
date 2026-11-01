package com.financial.modules.assessment.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Evaluation status for an individual financial ratio against its configured criterion")
public enum RatioEvaluationStatus {
    @Schema(description = "Financial ratio meets or exceeds the evaluation threshold")
    PASS,

    @Schema(description = "Financial ratio does not meet the evaluation threshold")
    FAIL,

    @Schema(description = "Required calculation value is missing, null, or calculation failed")
    INSUFFICIENT_DATA,

    @Schema(description = "No active evaluation criterion is configured for this ratio")
    NOT_CONFIGURED,

    @Schema(description = "Criterion is not applicable to the company or accounting context")
    NOT_APPLICABLE
}

