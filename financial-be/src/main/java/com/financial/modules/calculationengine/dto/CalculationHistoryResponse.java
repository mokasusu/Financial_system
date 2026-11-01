package com.financial.modules.calculationengine.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.financial.modules.calculationengine.enums.CalculationStatus;
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
@Schema(description = "Financial calculation history summary")
public class CalculationHistoryResponse {

    @Schema(description = "Calculation ID", example = "1")
    private Long id;

    @Schema(description = "Financial Statement ID", example = "10")
    private Long statementId;

    @Schema(description = "Calculation version sequence for this statement", example = "1")
    private Integer calculationVersion;

    @Schema(description = "Confirmed statement data version timestamp/tag", example = "2026-11-01T08:00:00")
    private String confirmedDataVersion;

    @Schema(description = "Overall calculation status", example = "SUCCESS")
    private CalculationStatus status;

    @Schema(description = "Overall status display name", example = "Thành công")
    private String statusDisplayName;

    @Schema(description = "Timestamp when calculation was performed")
    private LocalDateTime calculatedAt;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Total number of ratios calculated", example = "14")
    private Integer totalRatios;

    @Schema(description = "Number of successfully calculated ratios", example = "14")
    private Integer successfulRatios;
}

