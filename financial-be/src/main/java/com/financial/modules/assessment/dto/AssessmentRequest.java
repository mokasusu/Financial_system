package com.financial.modules.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for executing financial health assessment")
public class AssessmentRequest {

    @Schema(description = "Optional specific calculation execution ID. If omitted, the latest compatible calculation execution is automatically used.", example = "1")
    private Long calculationExecutionId;

    @Schema(description = "Optional specific validation execution ID. If omitted, the latest compatible validation execution is automatically used.", example = "1")
    private Long validationExecutionId;

    @Schema(description = "Optional assessment notes or business rationale", example = "Đánh giá sức khỏe tài chính định kỳ năm 2024")
    private String assessmentNotes;
}

