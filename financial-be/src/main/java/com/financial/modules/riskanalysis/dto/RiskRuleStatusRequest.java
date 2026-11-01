package com.financial.modules.riskanalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for activating or deactivating a risk rule")
public class RiskRuleStatusRequest {

    @NotNull(message = "Active status is required")
    @Schema(description = "Whether the risk rule should be active", example = "true")
    private Boolean isActive;
}

