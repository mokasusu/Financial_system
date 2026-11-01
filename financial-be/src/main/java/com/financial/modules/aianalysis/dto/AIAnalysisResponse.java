package com.financial.modules.aianalysis.dto;

import com.financial.modules.aianalysis.enums.AIAnalysisStatus;
import com.financial.modules.aianalysis.enums.AIReviewStatus;
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
@Schema(description = "Detailed AI financial health analysis response")
public class AIAnalysisResponse {

    @Schema(description = "AI Analysis ID", example = "1")
    private Long id;

    @Schema(description = "Associated assessment ID", example = "10")
    private Long assessmentId;

    @Schema(description = "Associated financial statement ID", example = "100")
    private Long financialStatementId;

    @Schema(description = "Company ID", example = "5")
    private Long companyId;

    @Schema(description = "Company name", example = "Công ty Cổ phần Công nghệ ABC")
    private String companyName;

    @Schema(description = "Analysis iteration version for this assessment", example = "1")
    private Integer analysisVersion;

    @Schema(description = "Confirmed financial data version", example = "v1")
    private String confirmedDataVersion;

    @Schema(description = "AI Provider used", example = "OPENROUTER")
    private String provider;

    @Schema(description = "AI Model used", example = "openrouter/free")
    private String model;

    @Schema(description = "Processing lifecycle status", example = "COMPLETED")
    private AIAnalysisStatus status;

    @Schema(description = "Localized status display name", example = "Hoàn thành phân tích")
    private String statusDisplayName;

    @Schema(description = "Review and approval status", example = "PENDING")
    private AIReviewStatus reviewStatus;

    @Schema(description = "Localized review status display name", example = "Chờ duyệt")
    private String reviewStatusDisplayName;

    @Schema(description = "Executive summary of enterprise financial health")
    private String executiveSummary;

    @Schema(description = "Original AI-generated comments (never overwritten)")
    private String aiGeneratedComment;

    @Schema(description = "User-approved or edited final commentary")
    private String finalComment;

    @Schema(description = "Error code if status is FAILED", example = "OPENROUTER_TIMEOUT")
    private String errorCode;

    @Schema(description = "Error message if status is FAILED")
    private String errorMessage;

    @Schema(description = "Analysis creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Analysis completion timestamp")
    private LocalDateTime completedAt;

    @Schema(description = "Identified financial strengths")
    private List<AIStrengthResponse> strengths;

    @Schema(description = "Identified financial risks")
    private List<AIRiskResponse> risks;

    @Schema(description = "Actionable financial improvement recommendations")
    private List<AIRecommendationResponse> recommendations;

    @Schema(description = "Analysis and data limitations")
    private List<AILimitationResponse> limitations;
}

