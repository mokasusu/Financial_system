package com.financial.modules.riskanalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for executing financial risk analysis")
public class RiskAnalysisRequest {

    @Schema(description = "Optional specific assessment ID. If omitted, the latest compatible assessment is automatically used.", example = "1")
    private Long assessmentId;

    @Schema(description = "Optional specific calculation execution ID. If omitted, compatible calculation from assessment is used.", example = "1")
    private Long calculationExecutionId;

    @Schema(description = "Optional specific validation execution ID. If omitted, compatible validation execution is used.", example = "1")
    private Long validationExecutionId;

    @Schema(description = "Optional notes or audit context for this risk analysis execution", example = "Phân tích rủi ro định kỳ Q4/2024")
    private String analysisNotes;
}

