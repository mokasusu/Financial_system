package com.financial.modules.riskanalysis.detector;

import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.assessment.enums.AssessmentStatus;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskRule;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import com.financial.modules.riskanalysis.mapper.RiskAnalysisMapper;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.enums.ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiskDetectorTest {

    private LiquidityRiskDetector liquidityDetector;
    private SolvencyRiskDetector solvencyDetector;
    private ProfitabilityRiskDetector profitabilityDetector;
    private EfficiencyRiskDetector efficiencyDetector;
    private DataQualityRiskDetector dataQualityDetector;

    private FinancialStatement statement;
    private FinancialCalculation calculation;
    private Assessment assessment;
    private ValidationExecution validation;
    private RiskAnalysisMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RiskAnalysisMapper();
        liquidityDetector = new LiquidityRiskDetector(mapper);
        solvencyDetector = new SolvencyRiskDetector(mapper);
        profitabilityDetector = new ProfitabilityRiskDetector(mapper);
        efficiencyDetector = new EfficiencyRiskDetector(mapper);
        dataQualityDetector = new DataQualityRiskDetector(mapper);

        statement = FinancialStatement.builder()
                .id(100L)
                .processingStatus(FinancialStatementStatus.DATA_CONFIRMED)
                .build();

        calculation = FinancialCalculation.builder()
                .id(200L)
                .financialStatement(statement)
                .results(new ArrayList<>())
                .build();

        assessment = Assessment.builder()
                .id(300L)
                .financialStatement(statement)
                .assessmentStatus(AssessmentStatus.COMPLETED)
                .build();

        validation = ValidationExecution.builder()
                .id(400L)
                .financialStatement(statement)
                .executionStatus(ValidationStatus.PASS)
                .failedRules(0)
                .warningRules(0)
                .build();
    }

    private FinancialCalculationResult buildCalcResult(String ratioCode, BigDecimal value) {
        return FinancialCalculationResult.builder()
                .ratioCode(ratioCode)
                .ratioName(ratioCode)
                .category(FinancialRatioCategory.LIQUIDITY)
                .unit(FinancialUnit.TIMES)
                .status(CalculationStatus.SUCCESS)
                .resultValue(value)
                .financialCalculation(calculation)
                .build();
    }

    private RiskRule buildRule(String ruleCode, String ruleName, RiskCategory category, String ratioCode,
                              ComparisonOperator op, BigDecimal threshold, RiskSeverity severity) {
        return RiskRule.builder()
                .id(1L)
                .ruleCode(ruleCode)
                .ruleName(ruleName)
                .riskCategory(category)
                .ratioCode(ratioCode)
                .comparisonOperator(op)
                .thresholdValue(threshold)
                .severity(severity)
                .explanationTemplate("Cảnh báo: Chỉ số {ratioCode} có giá trị {actualValue} {comparisonOperator} ngưỡng {thresholdValue}")
                .recommendationTemplate("Khuyến nghị: Xem xét cải thiện chỉ số {ratioCode}")
                .potentialImpactTemplate("Tác động tiềm tàng đến chỉ số {ratioCode}")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("LiquidityRiskDetector detects risk when current ratio is below threshold")
    void testLiquidityRiskDetection_Triggered() {
        RiskRule rule = buildRule("RULE_CR_LOW", "Current Ratio Thấp", RiskCategory.LIQUIDITY,
                "CURRENT_RATIO", ComparisonOperator.LESS_THAN, new BigDecimal("1.0000"), RiskSeverity.HIGH);

        calculation.getResults().add(buildCalcResult("CURRENT_RATIO", new BigDecimal("0.7500")));

        RiskDetectionContext ctx = RiskDetectionContext.builder()
                .statement(statement)
                .calculation(calculation)
                .assessment(assessment)
                .validationExecution(validation)
                .calculationResultsByCode(Collections.singletonMap("CURRENT_RATIO", calculation.getResults().get(0)))
                .previousRisks(Collections.emptyList())
                .build();

        List<IdentifiedRisk> risks = liquidityDetector.detect(ctx, List.of(rule));

        IdentifiedRisk risk = risks.stream()
                .filter(r -> "CURRENT_RATIO".equals(r.getRelatedRatioCode()))
                .findFirst().orElseThrow();
        assertThat(risk.getRiskCategory()).isEqualTo(RiskCategory.LIQUIDITY);
        assertThat(risk.getSeverity()).isEqualTo(RiskSeverity.HIGH);
        assertThat(risk.getRiskStatus()).isEqualTo(RiskStatus.DETECTED);
        assertThat(risk.getRuleCodeSnapshot()).isEqualTo("RULE_CR_LOW");
        assertThat(risk.getActualValue()).isEqualByComparingTo("0.7500");
        assertThat(risk.getThresholdValue()).isEqualByComparingTo("1.0000");
        assertThat(risk.getRiskDescription()).contains("CURRENT_RATIO").contains("0.7500").contains("<").contains("1.0000");
        assertThat(risk.getRecommendation()).contains("CURRENT_RATIO");
    }

    @Test
    @DisplayName("LiquidityRiskDetector returns INSUFFICIENT_DATA when calculated value is missing")
    void testLiquidityRiskDetection_InsufficientData() {
        RiskRule rule = buildRule("RULE_CR_LOW", "Current Ratio Thấp", RiskCategory.LIQUIDITY,
                "CURRENT_RATIO", ComparisonOperator.LESS_THAN, new BigDecimal("1.0000"), RiskSeverity.HIGH);

        // No calculation results
        RiskDetectionContext ctx = RiskDetectionContext.builder()
                .statement(statement)
                .calculation(calculation)
                .assessment(assessment)
                .validationExecution(validation)
                .calculationResultsByCode(Collections.emptyMap())
                .previousRisks(Collections.emptyList())
                .build();

        List<IdentifiedRisk> risks = liquidityDetector.detect(ctx, List.of(rule));

        IdentifiedRisk risk = risks.stream()
                .filter(r -> "CURRENT_RATIO".equals(r.getRelatedRatioCode()))
                .findFirst().orElseThrow();
        assertThat(risk.getRiskStatus()).isEqualTo(RiskStatus.INSUFFICIENT_DATA);
        assertThat(risk.getSeverity()).isEqualTo(RiskSeverity.HIGH);
        assertThat(risk.getRiskDescription()).contains("không đầy đủ");
    }

    @Test
    @DisplayName("LiquidityRiskDetector creates RESOLVED risk when previously detected risk improves")
    void testLiquidityRiskDetection_Resolved() {
        RiskRule rule = buildRule("RULE_CR_LOW", "Current Ratio Thấp", RiskCategory.LIQUIDITY,
                "CURRENT_RATIO", ComparisonOperator.LESS_THAN, new BigDecimal("1.0000"), RiskSeverity.HIGH);

        // Now value is 1.2500 (greater than threshold, so condition is FALSE)
        FinancialCalculationResult calcRes = buildCalcResult("CURRENT_RATIO", new BigDecimal("1.2500"));
        calculation.getResults().add(calcRes);

        IdentifiedRisk previousRisk = IdentifiedRisk.builder()
                .riskCategory(RiskCategory.LIQUIDITY)
                .ruleCodeSnapshot("RULE_CR_LOW")
                .relatedRatioCode("CURRENT_RATIO")
                .severity(RiskSeverity.HIGH)
                .riskStatus(RiskStatus.DETECTED)
                .detectedAt(LocalDateTime.now().minusDays(1))
                .build();

        RiskDetectionContext ctx = RiskDetectionContext.builder()
                .statement(statement)
                .calculation(calculation)
                .assessment(assessment)
                .validationExecution(validation)
                .calculationResultsByCode(Collections.singletonMap("CURRENT_RATIO", calcRes))
                .previousRisks(List.of(previousRisk))
                .build();

        List<IdentifiedRisk> risks = liquidityDetector.detect(ctx, List.of(rule));

        IdentifiedRisk risk = risks.stream()
                .filter(r -> "CURRENT_RATIO".equals(r.getRelatedRatioCode()))
                .findFirst().orElseThrow();
        assertThat(risk.getRiskStatus()).isEqualTo(RiskStatus.RESOLVED);
        assertThat(risk.getRiskDescription()).contains("không còn vi phạm");
        assertThat(risk.getActualValue()).isEqualByComparingTo("1.2500");
    }

    @Test
    @DisplayName("SolvencyRiskDetector returns NOT_CONFIGURED when no rule exists for debt ratio")
    void testSolvencyRiskDetection_NotConfigured() {
        FinancialCalculationResult calcRes = buildCalcResult("DEBT_TO_EQUITY", new BigDecimal("2.5000"));
        calculation.getResults().add(calcRes);

        RiskDetectionContext ctx = RiskDetectionContext.builder()
                .statement(statement)
                .calculation(calculation)
                .assessment(assessment)
                .validationExecution(validation)
                .calculationResultsByCode(Collections.singletonMap("DEBT_TO_EQUITY", calcRes))
                .previousRisks(Collections.emptyList())
                .build();

        List<IdentifiedRisk> risks = solvencyDetector.detect(ctx, Collections.emptyList());

        // Handled ratios: DEBT_TO_EQUITY, DEBT_RATIO, EQUITY_RATIO -> all NOT_CONFIGURED
        assertThat(risks).hasSize(3);
        assertThat(risks).allMatch(r -> r.getRiskStatus() == RiskStatus.NOT_CONFIGURED);
    }

    @Test
    @DisplayName("SolvencyRiskDetector detects CRITICAL risk when DEBT_TO_EQUITY exceeds threshold")
    void testSolvencyRiskDetection_Triggered() {
        RiskRule rule = buildRule("RULE_DTE_HIGH", "Nợ trên VCSH cao", RiskCategory.SOLVENCY,
                "DEBT_TO_EQUITY", ComparisonOperator.GREATER_THAN, new BigDecimal("2.0000"), RiskSeverity.CRITICAL);

        FinancialCalculationResult calcRes = buildCalcResult("DEBT_TO_EQUITY", new BigDecimal("3.1000"));
        calculation.getResults().add(calcRes);

        RiskDetectionContext ctx = RiskDetectionContext.builder()
                .statement(statement)
                .calculation(calculation)
                .assessment(assessment)
                .validationExecution(validation)
                .calculationResultsByCode(Collections.singletonMap("DEBT_TO_EQUITY", calcRes))
                .previousRisks(Collections.emptyList())
                .build();

        List<IdentifiedRisk> risks = solvencyDetector.detect(ctx, List.of(rule));

        assertThat(risks).isNotEmpty();
        IdentifiedRisk dteRisk = risks.stream()
                .filter(r -> "DEBT_TO_EQUITY".equals(r.getRelatedRatioCode()))
                .findFirst().orElseThrow();

        assertThat(dteRisk.getRiskStatus()).isEqualTo(RiskStatus.DETECTED);
        assertThat(dteRisk.getSeverity()).isEqualTo(RiskSeverity.CRITICAL);
        assertThat(dteRisk.getActualValue()).isEqualByComparingTo("3.1000");
    }

    @Test
    @DisplayName("ProfitabilityRiskDetector detects MEDIUM risk when ROA is less than threshold")
    void testProfitabilityRiskDetection_Triggered() {
        RiskRule rule = buildRule("RULE_ROA_LOW", "ROA Thấp", RiskCategory.PROFITABILITY,
                "ROA", ComparisonOperator.LESS_THAN, new BigDecimal("5.0000"), RiskSeverity.MEDIUM);

        FinancialCalculationResult calcRes = buildCalcResult("ROA", new BigDecimal("2.3000"));
        calculation.getResults().add(calcRes);

        RiskDetectionContext ctx = RiskDetectionContext.builder()
                .statement(statement)
                .calculation(calculation)
                .assessment(assessment)
                .validationExecution(validation)
                .calculationResultsByCode(Collections.singletonMap("ROA", calcRes))
                .previousRisks(Collections.emptyList())
                .build();

        List<IdentifiedRisk> risks = profitabilityDetector.detect(ctx, List.of(rule));

        IdentifiedRisk roaRisk = risks.stream()
                .filter(r -> "ROA".equals(r.getRelatedRatioCode()))
                .findFirst().orElseThrow();

        assertThat(roaRisk.getRiskStatus()).isEqualTo(RiskStatus.DETECTED);
        assertThat(roaRisk.getSeverity()).isEqualTo(RiskSeverity.MEDIUM);
        assertThat(roaRisk.getActualValue()).isEqualByComparingTo("2.3000");
    }

    @Test
    @DisplayName("EfficiencyRiskDetector detects risk when ASSET_TURNOVER is below threshold")
    void testEfficiencyRiskDetection_Triggered() {
        RiskRule rule = buildRule("RULE_AT_LOW", "Vòng quay tài sản thấp", RiskCategory.OPERATIONAL_EFFICIENCY,
                "ASSET_TURNOVER", ComparisonOperator.LESS_THAN, new BigDecimal("0.8000"), RiskSeverity.LOW);

        FinancialCalculationResult calcRes = buildCalcResult("ASSET_TURNOVER", new BigDecimal("0.4500"));
        calculation.getResults().add(calcRes);

        RiskDetectionContext ctx = RiskDetectionContext.builder()
                .statement(statement)
                .calculation(calculation)
                .assessment(assessment)
                .validationExecution(validation)
                .calculationResultsByCode(Collections.singletonMap("ASSET_TURNOVER", calcRes))
                .previousRisks(Collections.emptyList())
                .build();

        List<IdentifiedRisk> risks = efficiencyDetector.detect(ctx, List.of(rule));

        IdentifiedRisk atRisk = risks.stream()
                .filter(r -> "ASSET_TURNOVER".equals(r.getRelatedRatioCode()))
                .findFirst().orElseThrow();

        assertThat(atRisk.getRiskStatus()).isEqualTo(RiskStatus.DETECTED);
        assertThat(atRisk.getSeverity()).isEqualTo(RiskSeverity.LOW);
    }

    @Test
    @DisplayName("DataQualityRiskDetector creates risk when validation failed or has errors")
    void testDataQualityRiskDetector_ErrorsFound() {
        validation.setExecutionStatus(ValidationStatus.FAIL);
        validation.setFailedRules(2);

        RiskRule rule = buildRule("RULE_DQ_FAIL", "Lỗi dữ liệu BCTC", RiskCategory.DATA_QUALITY,
                null, ComparisonOperator.GREATER_THAN, BigDecimal.ZERO, RiskSeverity.HIGH);

        RiskDetectionContext ctx = RiskDetectionContext.builder()
                .statement(statement)
                .calculation(calculation)
                .assessment(assessment)
                .validationExecution(validation)
                .calculationResultsByCode(Collections.emptyMap())
                .previousRisks(Collections.emptyList())
                .build();

        List<IdentifiedRisk> risks = dataQualityDetector.detect(ctx, List.of(rule));

        assertThat(risks).isNotEmpty();
        IdentifiedRisk risk = risks.get(0);
        assertThat(risk.getRiskCategory()).isEqualTo(RiskCategory.DATA_QUALITY);
        assertThat(risk.getSeverity()).isEqualTo(RiskSeverity.HIGH);
        assertThat(risk.getRiskStatus()).isEqualTo(RiskStatus.DETECTED);
    }

    @Test
    @DisplayName("DataQualityRiskDetector returns INSUFFICIENT_DATA when validation is null")
    void testDataQualityRiskDetector_NoValidation() {
        RiskRule rule = buildRule("RULE_DQ_FAIL", "Lỗi dữ liệu BCTC", RiskCategory.DATA_QUALITY,
                null, ComparisonOperator.GREATER_THAN, BigDecimal.ZERO, RiskSeverity.HIGH);

        RiskDetectionContext ctx = RiskDetectionContext.builder()
                .statement(statement)
                .calculation(calculation)
                .assessment(assessment)
                .validationExecution(null)
                .calculationResultsByCode(Collections.emptyMap())
                .previousRisks(Collections.emptyList())
                .build();

        List<IdentifiedRisk> risks = dataQualityDetector.detect(ctx, List.of(rule));

        assertThat(risks).hasSize(1);
        assertThat(risks.get(0).getRiskStatus()).isEqualTo(RiskStatus.INSUFFICIENT_DATA);
    }
}
