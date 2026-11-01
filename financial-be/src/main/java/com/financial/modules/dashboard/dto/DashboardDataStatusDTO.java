package com.financial.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Availability and processing statuses of upstream financial modules")
public class DashboardDataStatusDTO {

    @Schema(description = "Company module status", example = "READY")
    private String companyStatus;

    @Schema(description = "Financial statement processing status", example = "READY")
    private String statementStatus;

    @Schema(description = "Calculation engine status", example = "READY")
    private String calculationStatus;

    @Schema(description = "Validation engine status", example = "READY")
    private String validationStatus;

    @Schema(description = "Assessment module status", example = "READY")
    private String assessmentStatus;

    @Schema(description = "Risk analysis status", example = "READY")
    private String riskStatus;

    @Schema(description = "AI analysis commentary status", example = "READY")
    private String aiStatus;
}

