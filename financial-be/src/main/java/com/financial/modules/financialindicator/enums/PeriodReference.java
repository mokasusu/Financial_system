package com.financial.modules.financialindicator.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Reporting period balance reference for ratio input dependencies")
public enum PeriodReference {
    @Schema(description = "Current reporting period")
    CURRENT,

    @Schema(description = "Beginning balance of the reporting period (Đầu kỳ)")
    BEGINNING,

    @Schema(description = "Ending balance of the reporting period (Cuối kỳ)")
    ENDING,

    @Schema(description = "Average balance over the period ((Đầu kỳ + Cuối kỳ) / 2)")
    AVERAGE
}

