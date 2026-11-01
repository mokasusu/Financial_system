package com.financial.modules.financialindicator.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Evaluation direction indicating whether higher or lower values are financially desirable")
public enum EvaluationDirection {
    @Schema(description = "Higher value represents better financial health")
    HIGHER_IS_BETTER,

    @Schema(description = "Lower value represents better financial health (e.g. debt ratios)")
    LOWER_IS_BETTER,

    @Schema(description = "Evaluation depends on industry context or specific thresholds")
    CONTEXT_DEPENDENT
}

