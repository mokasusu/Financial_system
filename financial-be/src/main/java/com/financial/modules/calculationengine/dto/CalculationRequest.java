package com.financial.modules.calculationengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Optional calculation execution parameters")
public class CalculationRequest {

    @Schema(description = "Optional custom calculation scale override (default null to use system configuration)", example = "4")
    private Integer decimalScale;
}

