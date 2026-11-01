package com.financial.modules.aianalysis.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.modules.aianalysis.dto.*;
import com.financial.modules.aianalysis.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class AIAnalysisMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIAnalysisResponse toAnalysisResponse(AIAnalysis entity) {
        if (entity == null) return null;

        Long assessmentId = entity.getAssessment() != null ? entity.getAssessment().getId() : null;
        Long statementId = entity.getFinancialStatement() != null ? entity.getFinancialStatement().getId() : null;
        Long companyId = entity.getCompany() != null ? entity.getCompany().getId() : null;
        String companyName = entity.getCompany() != null ? entity.getCompany().getCompanyName() : null;

        List<AIStrengthResponse> strengths = entity.getStrengths() != null
                ? entity.getStrengths().stream().map(this::toStrengthResponse).toList()
                : Collections.emptyList();

        List<AIRiskResponse> risks = entity.getRisks() != null
                ? entity.getRisks().stream().map(this::toRiskResponse).toList()
                : Collections.emptyList();

        List<AIRecommendationResponse> recommendations = entity.getRecommendations() != null
                ? entity.getRecommendations().stream().map(this::toRecommendationResponse).toList()
                : Collections.emptyList();

        List<AILimitationResponse> limitations = entity.getLimitations() != null
                ? entity.getLimitations().stream().map(this::toLimitationResponse).toList()
                : Collections.emptyList();

        return AIAnalysisResponse.builder()
                .id(entity.getId())
                .assessmentId(assessmentId)
                .financialStatementId(statementId)
                .companyId(companyId)
                .companyName(companyName)
                .analysisVersion(entity.getAnalysisVersion())
                .confirmedDataVersion(entity.getConfirmedDataVersion())
                .provider(entity.getProvider())
                .model(entity.getModel())
                .status(entity.getStatus())
                .statusDisplayName(entity.getStatus() != null ? entity.getStatus().getDisplayName() : null)
                .reviewStatus(entity.getReviewStatus())
                .reviewStatusDisplayName(entity.getReviewStatus() != null ? entity.getReviewStatus().getDisplayName() : null)
                .executiveSummary(entity.getExecutiveSummary())
                .aiGeneratedComment(entity.getAiGeneratedComment())
                .finalComment(entity.getFinalComment())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .strengths(strengths)
                .risks(risks)
                .recommendations(recommendations)
                .limitations(limitations)
                .build();
    }

    public AIAnalysisHistoryResponse toHistoryResponse(AIAnalysis entity) {
        if (entity == null) return null;

        Long assessmentId = entity.getAssessment() != null ? entity.getAssessment().getId() : null;

        return AIAnalysisHistoryResponse.builder()
                .id(entity.getId())
                .assessmentId(assessmentId)
                .analysisVersion(entity.getAnalysisVersion())
                .confirmedDataVersion(entity.getConfirmedDataVersion())
                .provider(entity.getProvider())
                .model(entity.getModel())
                .status(entity.getStatus())
                .reviewStatus(entity.getReviewStatus())
                .executiveSummary(entity.getExecutiveSummary())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    public AIStrengthResponse toStrengthResponse(AIAnalysisStrength strength) {
        if (strength == null) return null;

        List<String> evidence = deserializeEvidence(strength.getEvidence());

        return AIStrengthResponse.builder()
                .id(strength.getId())
                .title(strength.getTitle())
                .description(strength.getDescription())
                .evidence(evidence)
                .build();
    }

    public AIRiskResponse toRiskResponse(AIAnalysisRisk risk) {
        if (risk == null) return null;

        List<String> evidence = deserializeEvidence(risk.getEvidence());

        return AIRiskResponse.builder()
                .id(risk.getId())
                .title(risk.getTitle())
                .description(risk.getDescription())
                .severity(risk.getSeverity())
                .severityDisplayName(risk.getSeverity() != null ? risk.getSeverity().getDisplayName() : null)
                .category(risk.getCategory())
                .categoryDisplayName(risk.getCategory() != null ? risk.getCategory().getDisplayName() : null)
                .evidence(evidence)
                .sourceRiskId(risk.getSourceRiskId())
                .build();
    }

    public AIRecommendationResponse toRecommendationResponse(AIAnalysisRecommendation rec) {
        if (rec == null) return null;

        return AIRecommendationResponse.builder()
                .id(rec.getId())
                .title(rec.getTitle())
                .description(rec.getDescription())
                .priority(rec.getPriority())
                .priorityDisplayName(rec.getPriority() != null ? rec.getPriority().getDisplayName() : null)
                .relatedRisk(rec.getRelatedRisk())
                .relatedRiskId(rec.getRelatedRiskId())
                .build();
    }

    public AILimitationResponse toLimitationResponse(AIAnalysisLimitation lim) {
        if (lim == null) return null;

        return AILimitationResponse.builder()
                .id(lim.getId())
                .description(lim.getDescription())
                .build();
    }

    public String serializeEvidence(List<String> evidenceList) {
        if (evidenceList == null || evidenceList.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(evidenceList);
        } catch (Exception ex) {
            log.warn("Failed to serialize evidence list to JSON: {}", ex.getMessage());
            return String.join("; ", evidenceList);
        }
    }

    public List<String> deserializeEvidence(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            return List.of(json);
        }
    }
}

