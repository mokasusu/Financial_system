package com.financial.modules.validation.dto;

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
@Schema(description = "Request payload to activate or deactivate a validation rule")
public class ValidationRuleStatusRequest {

    @NotNull(message = "Active status is required")
    @Schema(description = "Target active status (true to activate, false to deactivate)", example = "true")
    private Boolean isActive;
}

