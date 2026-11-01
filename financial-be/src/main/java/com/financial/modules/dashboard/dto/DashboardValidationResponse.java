package com.financial.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Validation overview summary and detailed rule evaluation results")
public class DashboardValidationResponse {

    @Schema(description = "Validation execution ID", example = "501")
    private Long validationId;

    @Schema(description = "Overall validation status", example = "PASS")
    private String overallStatus;

    @Schema(description = "Accounting standard applied", example = "TT200")
    private String accountingStandard;

    @Schema(description = "Total rules evaluated", example = "8")
    private Integer totalRules;

    @Schema(description = "Number of passed rules", example = "7")
    private Integer passedRules;

    @Schema(description = "Number of warning rules", example = "1")
    private Integer warningRules;

    @Schema(description = "Number of failed rules", example = "0")
    private Integer failedRules;

    @Schema(description = "Number of review-required rules", example = "0")
    private Integer reviewRequiredRules;

    @Schema(description = "Number of error rules", example = "0")
    private Integer errorRules;

    @Schema(description = "Number of not-applicable rules", example = "0")
    private Integer notApplicableRules;

    @Schema(description = "Execution timestamp")
    private LocalDateTime executedAt;

    @Schema(description = "Detailed list of validation rule results")
    private List<ValidationResultItemDTO> results;
}

