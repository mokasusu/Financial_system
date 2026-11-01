package com.financial.modules.aianalysis.dto.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AIAnalysisInputPayload {

    private CompanyInfo company;
    private List<RatioInfo> financialRatios;
    private AssessmentInfo assessment;
    private ValidationInfo validation;
    private List<RiskInfo> detectedRisks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfo {
        private String companyName;
        private String industry;
        private String accountingStandard;
        private String period;
        private String currencyUnit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatioInfo {
        private String ratioCode;
        private String ratioName;
        private BigDecimal actualValue;
        private String unit;
        private String calculationStatus;
        private String evaluationStatus;
        private String missingDataExplanation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentInfo {
        private Integer assessmentVersion;
        private String confirmedDataVersion;
        private String assessmentStatus;
        private String scoringStatus;
        private BigDecimal overallScore;
        private String healthClassification;
        private String dataQualityStatus;
        private List<CriterionInfo> criteriaEvaluations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CriterionInfo {
        private String criterionName;
        private String ratioCode;
        private String comparisonOperator;
        private BigDecimal thresholdValue;
        private String evaluationStatus;
        private String explanation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationInfo {
        private String executionStatus;
        private String accountingStandard;
        private Integer totalRules;
        private Integer passedRules;
        private Integer failedRules;
        private Integer warningRules;
        private List<String> errors;
        private List<String> warnings;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskInfo {
        private Long riskId;
        private String category;
        private String riskTitle;
        private String description;
        private String severity;
        private String status;
        private String relatedRatioCode;
        private BigDecimal actualValue;
        private BigDecimal thresholdValue;
        private String existingRecommendation;
    }
}

