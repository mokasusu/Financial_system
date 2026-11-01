package com.financial.modules.validation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Optional validation execution parameters")
public class ValidationRequest {

    @Schema(description = "Optional override tolerance amount", example = "1000.0000")
    private java.math.BigDecimal toleranceOverride;
}

