package com.financial.modules.aianalysis.service;

import com.financial.modules.aianalysis.dto.internal.AIAnalysisInputPayload;
import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.assessment.entity.AssessmentDetail;
import com.financial.modules.assessment.enums.AssessmentScoringStatus;
import com.financial.modules.assessment.enums.AssessmentStatus;
import com.financial.modules.assessment.enums.DataQualityStatus;
import com.financial.modules.assessment.enums.HealthClassification;
import com.financial.modules.assessment.enums.RatioEvaluationStatus;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.company.entity.Company;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskAnalysis;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.validation.enums.ValidationSeverity;
import com.financial.modules.validation.enums.ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AIAnalysisPayloadBuilderTest {

    private AIAnalysisPayloadBuilder payloadBuilder;

    @BeforeEach
    void setUp() {
        payloadBuilder = new AIAnalysisPayloadBuilder();
    }

    @Test
    @DisplayName("buildPayload correctly maps company, all 14 ratios, assessment, validation, and risks")
    void testBuildPayload_Success() {
        Company company = Company.builder()
                .id(1L)
                .companyName("Công ty ABC")
                .taxCode("0101234567")
                .mainBusinessLine("Công nghệ phần mềm")
                .build();

        FinancialStatement statement = FinancialStatement.builder()
                .id(100L)
                .company(company)
                .accountingStandard("TT200")
                .period("2025")
                .unit("VND")
                .processingStatus(FinancialStatementStatus.DATA_CONFIRMED)
                .build();

        FinancialCalculationResult cr1 = FinancialCalculationResult.builder()
                .ratioCode("CURRENT_RATIO")
                .ratioName("Hệ số khả năng thanh toán hiện hành")
                .resultValue(new BigDecimal("1.8500"))
                .unit(FinancialUnit.TIMES)
                .status(CalculationStatus.SUCCESS)
                .build();

        FinancialCalculation calculation = FinancialCalculation.builder()
                .id(200L)
                .financialStatement(statement)
                .results(List.of(cr1))
                .build();

        AssessmentDetail ad1 = AssessmentDetail.builder()
                .ratioCodeSnapshot("CURRENT_RATIO")
                .ratioNameSnapshot("Hệ số khả năng thanh toán hiện hành")
                .criterionSnapshot("Chuẩn khả năng thanh toán hiện hành")
                .comparisonOperatorSnapshot(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .thresholdValueSnapshot(new BigDecimal("1.5000"))
                .evaluationStatus(RatioEvaluationStatus.PASS)
                .evaluationExplanation("Chỉ số đạt chuẩn")
                .build();

        Assessment assessment = Assessment.builder()
                .id(300L)
                .financialStatement(statement)
                .company(company)
                .assessmentVersion(1)
                .confirmedDataVersion("v1")
                .assessmentStatus(AssessmentStatus.COMPLETED)
                .scoringStatus(AssessmentScoringStatus.NOT_CONFIGURED)
                .overallScore(null)
                .healthClassification(HealthClassification.NOT_CONFIGURED)
                .dataQualityStatus(DataQualityStatus.VALID)
                .details(List.of(ad1))
                .build();

        ValidationResult vr1 = ValidationResult.builder()
                .ruleCodeSnapshot("RULE_WARNING_01")
                .ruleNameSnapshot("Biến động chi phí quản lý")
                .explanation("Chi phí quản lý tăng hơn 50% so với cùng kỳ")
                .status(ValidationStatus.WARNING)
                .severity(ValidationSeverity.MEDIUM)
                .build();

        ValidationExecution validation = ValidationExecution.builder()
                .id(400L)
                .executionStatus(ValidationStatus.PASS)
                .accountingStandard(AccountingStandard.TT200)
                .totalRules(5)
                .passedRules(4)
                .warningRules(1)
                .failedRules(0)
                .results(List.of(vr1))
                .build();

        IdentifiedRisk risk1 = IdentifiedRisk.builder()
                .id(501L)
                .riskCategory(RiskCategory.LIQUIDITY)
                .riskTitle("Thanh toán nhanh cần theo dõi")
                .riskDescription("Quick ratio xấp xỉ ngưỡng")
                .severity(RiskSeverity.MEDIUM)
                .riskStatus(RiskStatus.MONITORING)
                .relatedRatioCode("QUICK_RATIO")
                .actualValue(new BigDecimal("0.9500"))
                .thresholdValue(new BigDecimal("1.0000"))
                .recommendation("Cân nhắc tăng tiền gửi ngắn hạn")
                .build();

        RiskAnalysis riskAnalysis = RiskAnalysis.builder()
                .id(500L)
                .financialStatement(statement)
                .identifiedRisks(List.of(risk1))
                .build();

        AIAnalysisInputPayload payload = payloadBuilder.buildPayload(
                assessment, statement, company, calculation, validation, riskAnalysis
        );

        assertThat(payload).isNotNull();

        // 1. Company
        assertThat(payload.getCompany().getCompanyName()).isEqualTo("Công ty ABC");
        assertThat(payload.getCompany().getIndustry()).isEqualTo("Công nghệ phần mềm");
        assertThat(payload.getCompany().getAccountingStandard()).isEqualTo("TT200");

        // 2. 14 Ratios
        assertThat(payload.getFinancialRatios()).hasSize(14);
        AIAnalysisInputPayload.RatioInfo currentRatio = payload.getFinancialRatios().stream()
                .filter(r -> "CURRENT_RATIO".equals(r.getRatioCode()))
                .findFirst().orElseThrow();
        assertThat(currentRatio.getActualValue()).isEqualByComparingTo("1.8500");
        assertThat(currentRatio.getCalculationStatus()).isEqualTo("SUCCESS");
        assertThat(currentRatio.getEvaluationStatus()).isEqualTo("PASS");

        // Missing ratio QUICK_RATIO
        AIAnalysisInputPayload.RatioInfo quickRatio = payload.getFinancialRatios().stream()
                .filter(r -> "QUICK_RATIO".equals(r.getRatioCode()))
                .findFirst().orElseThrow();
        assertThat(quickRatio.getActualValue()).isNull();
        assertThat(quickRatio.getCalculationStatus()).isEqualTo("INSUFFICIENT_DATA");
        assertThat(quickRatio.getMissingDataExplanation()).isNotBlank();

        // 3. Assessment
        assertThat(payload.getAssessment().getAssessmentVersion()).isEqualTo(1);
        assertThat(payload.getAssessment().getConfirmedDataVersion()).isEqualTo("v1");
        assertThat(payload.getAssessment().getScoringStatus()).isEqualTo("NOT_CONFIGURED");
        assertThat(payload.getAssessment().getOverallScore()).isNull();
        assertThat(payload.getAssessment().getCriteriaEvaluations()).hasSize(1);

        // 4. Validation
        assertThat(payload.getValidation()).isNotNull();
        assertThat(payload.getValidation().getWarningRules()).isEqualTo(1);
        assertThat(payload.getValidation().getWarnings()).hasSize(1);
        assertThat(payload.getValidation().getWarnings().get(0)).contains("RULE_WARNING_01");

        // 5. Risks
        assertThat(payload.getDetectedRisks()).hasSize(1);
        assertThat(payload.getDetectedRisks().get(0).getRiskTitle()).isEqualTo("Thanh toán nhanh cần theo dõi");
        assertThat(payload.getDetectedRisks().get(0).getSeverity()).isEqualTo("MEDIUM");
    }
}
