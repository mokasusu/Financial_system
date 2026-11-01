package com.financial.modules.validation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.validation.enums.ValidationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Validation execution history summary")
public class ValidationHistoryResponse {

    @Schema(description = "Validation execution ID", example = "1")
    private Long id;

    @Schema(description = "Financial statement ID", example = "10")
    private Long statementId;

    @Schema(description = "Confirmed statement data version timestamp/tag", example = "2026-11-01T08:00:00")
    private String confirmedDataVersion;

    @Schema(description = "Accounting standard applied for validation", example = "TT200")
    private AccountingStandard accountingStandard;

    @Schema(description = "Overall validation outcome status", example = "PASS")
    private ValidationStatus executionStatus;

    @Schema(description = "Overall status display name", example = "Đạt")
    private String executionStatusDisplayName;

    @Schema(description = "Total rules executed", example = "12")
    private Integer totalRules;

    @Schema(description = "Passed rules count", example = "10")
    private Integer passedRules;

    @Schema(description = "Warning rules count", example = "2")
    private Integer warningRules;

    @Schema(description = "Failed rules count", example = "0")
    private Integer failedRules;

    @Schema(description = "Review required rules count", example = "0")
    private Integer reviewRequiredRules;

    @Schema(description = "Timestamp when validation was executed")
    private LocalDateTime executedAt;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;
}

