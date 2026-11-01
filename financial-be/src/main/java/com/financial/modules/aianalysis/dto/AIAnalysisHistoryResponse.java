package com.financial.modules.aianalysis.dto;

import com.financial.modules.aianalysis.enums.AIAnalysisStatus;
import com.financial.modules.aianalysis.enums.AIReviewStatus;
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
@Schema(description = "Summary record for AI analysis history list")
public class AIAnalysisHistoryResponse {

    @Schema(description = "AI Analysis ID", example = "1")
    private Long id;

    @Schema(description = "Assessment ID", example = "10")
    private Long assessmentId;

    @Schema(description = "Analysis iteration version", example = "1")
    private Integer analysisVersion;

    @Schema(description = "Confirmed data version", example = "v1")
    private String confirmedDataVersion;

    @Schema(description = "AI Provider", example = "OPENROUTER")
    private String provider;

    @Schema(description = "AI Model", example = "openrouter/free")
    private String model;

    @Schema(description = "Processing status", example = "COMPLETED")
    private AIAnalysisStatus status;

    @Schema(description = "Review status", example = "PENDING")
    private AIReviewStatus reviewStatus;

    @Schema(description = "Executive summary")
    private String executiveSummary;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Completion timestamp")
    private LocalDateTime completedAt;
}

