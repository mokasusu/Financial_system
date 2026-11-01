package com.financial.modules.aianalysis.dto.internal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIParsedAnalysisOutput {

    private String executiveSummary;
    private List<StrengthItem> strengths;
    private List<RiskItem> risks;
    private List<RecommendationItem> recommendations;
    private List<String> limitations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StrengthItem {
        private String title;
        private String description;
        private List<String> evidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RiskItem {
        @JsonAlias({"sourceRiskId", "source_risk_id", "riskId", "risk_id"})
        private String sourceRiskId;
        private String title;
        private String description;
        private String severity;
        private String category;
        private List<String> evidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecommendationItem {
        private String title;
        private String description;
        private String priority;
        @JsonAlias({"relatedRiskId", "related_risk_id", "relatedRisk", "related_risk"})
        private String relatedRiskId;

        // Legacy / fallback getter for compatibility
        public String getRelatedRisk() {
            return relatedRiskId;
        }
    }
}
