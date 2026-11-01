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
@Schema(description = "AI financial health analysis executive commentary")
public class DashboardAICommentaryResponse {

    @Schema(description = "AI analysis ID", example = "201")
    private Long analysisId;

    @Schema(description = "Executive financial health summary")
    private String executiveSummary;

    @Schema(description = "Identified financial strengths")
    private List<DashboardAIStrengthDTO> strengths;

    @Schema(description = "Identified financial risks")
    private List<DashboardAIRiskDTO> risks;

    @Schema(description = "Actionable strategic recommendations")
    private List<DashboardAIRecommendationDTO> recommendations;

    @Schema(description = "Analytical limitations and data constraints")
    private List<String> limitations;

    @Schema(description = "AI generation status", example = "COMPLETED")
    private String aiGenerationStatus;

    @Schema(description = "User review status", example = "APPROVED")
    private String reviewStatus;

    @Schema(description = "Original immutable AI generated commentary (JSON/text)")
    private String aiGeneratedComment;

    @Schema(description = "User-approved or edited final commentary")
    private String finalComment;

    @Schema(description = "Creation timestamp")
    private LocalDateTime generatedAt;

    @Schema(description = "Completion timestamp")
    private LocalDateTime completedAt;
}

