package com.financial.modules.company.dto;

import com.financial.modules.company.enums.CompanyStatus;
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
@Schema(description = "Company status update payload")
public class CompanyStatusUpdateRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "Target operating status", example = "INACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    private CompanyStatus status;
}

