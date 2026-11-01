package com.financial.modules.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.exception.StatementNotFoundException;
import com.financial.modules.aianalysis.entity.AIAnalysis;
import com.financial.modules.aianalysis.enums.AIAnalysisStatus;
import com.financial.modules.aianalysis.enums.AIReviewStatus;
import com.financial.modules.aianalysis.repository.AIAnalysisRepository;
import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.assessment.entity.AssessmentDetail;
import com.financial.modules.assessment.enums.RatioEvaluationStatus;
import com.financial.modules.assessment.repository.AssessmentRepository;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.calculationengine.repository.FinancialCalculationRepository;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.dashboard.dto.*;
import com.financial.modules.dashboard.service.impl.DashboardServiceImpl;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.evaluationcriteria.repository.EvaluationCriteriaRepository;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.financialstatement.repository.FinancialStatementRepository;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskAnalysis;
import com.financial.modules.riskanalysis.enums.RiskAnalysisStatus;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import com.financial.modules.riskanalysis.repository.RiskAnalysisRepository;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationSeverity;
import com.financial.modules.validation.enums.ValidationStatus;
import com.financial.modules.validation.repository.ValidationExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private FinancialStatementRepository statementRepository;
    @Mock
    private FinancialCalculationRepository calculationRepository;
    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private ValidationExecutionRepository validationExecutionRepository;
    @Mock
    private RiskAnalysisRepository riskAnalysisRepository;
    @Mock
    private AIAnalysisRepository aiAnalysisRepository;
    @Mock
    private EvaluationCriteriaRepository evaluationCriteriaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private DashboardServiceImpl dashboardService;

    private Company company;
    private FinancialStatement confirmedStatement;
    private FinancialCalculation calculation;
    private Assessment assessment;
    private ValidationExecution validationExecution;
    private RiskAnalysis riskAnalysis;
    private AIAnalysis aiAnalysis;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(
                companyRepository,
                statementRepository,
                calculationRepository,
                assessmentRepository,
                validationExecutionRepository,
                riskAnalysisRepository,
                aiAnalysisRepository,
                evaluationCriteriaRepository,
                objectMapper
        );

        company = Company.builder()
                .id(1L)
                .companyName("Công ty Cổ phần ABC")
                .taxCode("0101234567")
                .mainBusinessLine("Công nghệ thông tin")
                .build();

        FinancialStatementItem itemAssets = FinancialStatementItem.builder()
                .code("270")
                .key("TOTAL_ASSETS")
                .statementType(FinancialStatementType.BALANCE_SHEET)
                .currentValue(new BigDecimal("10000000000"))
                .build();
        FinancialStatementItem itemLiab = FinancialStatementItem.builder()
                .code("300")
                .key("TOTAL_LIABILITIES")
                .statementType(FinancialStatementType.BALANCE_SHEET)
                .currentValue(new BigDecimal("4000000000"))
                .build();
        FinancialStatementItem itemEquity = FinancialStatementItem.builder()
                .code("400")
                .key("EQUITY")
                .statementType(FinancialStatementType.BALANCE_SHEET)
                .currentValue(new BigDecimal("6000000000"))
                .build();

        confirmedStatement = FinancialStatement.builder()
                .id(10L)
                .company(company)
                .period("2024")
                .fiscalYear(2024)
                .accountingStandard("TT200")
                .unit("VND")
                .processingStatus(FinancialStatementStatus.DATA_CONFIRMED)
                .items(List.of(itemAssets, itemLiab, itemEquity))
                .build();

        // 14 ratios calculation results
        List<FinancialCalculationResult> calcResults = new ArrayList<>();
        calcResults.add(FinancialCalculationResult.builder()
                .ratioCode("CURRENT_RATIO").ratioName("Khả năng thanh toán hiện hành")
                .category(FinancialRatioCategory.LIQUIDITY).resultValue(new BigDecimal("1.8500"))
                .unit(FinancialUnit.TIMES).status(CalculationStatus.SUCCESS).build());
        calcResults.add(FinancialCalculationResult.builder()
                .ratioCode("QUICK_RATIO").ratioName("Khả năng thanh toán nhanh")
                .category(FinancialRatioCategory.LIQUIDITY).resultValue(new BigDecimal("1.2000"))
                .unit(FinancialUnit.TIMES).status(CalculationStatus.SUCCESS).build());
        calcResults.add(FinancialCalculationResult.builder()
                .ratioCode("CASH_RATIO").ratioName("Khả năng thanh toán tức thời")
                .category(FinancialRatioCategory.LIQUIDITY).resultValue(new BigDecimal("0.5000"))
                .unit(FinancialUnit.TIMES).status(CalculationStatus.SUCCESS).build());
        calcResults.add(FinancialCalculationResult.builder()
                .ratioCode("DEBT_TO_EQUITY").ratioName("Hệ số nợ trên vốn CSH")
                .category(FinancialRatioCategory.SOLVENCY).resultValue(new BigDecimal("0.6667"))
                .unit(FinancialUnit.TIMES).status(CalculationStatus.SUCCESS).build());
        calcResults.add(FinancialCalculationResult.builder()
                .ratioCode("DEBT_RATIO").ratioName("Hệ số nợ")
                .category(FinancialRatioCategory.SOLVENCY).resultValue(new BigDecimal("0.4000"))
                .unit(FinancialUnit.TIMES).status(CalculationStatus.SUCCESS).build());
        calcResults.add(FinancialCalculationResult.builder()
                .ratioCode("EQUITY_RATIO").ratioName("Hệ số vốn CSH")
                .category(FinancialRatioCategory.SOLVENCY).resultValue(new BigDecimal("0.6000"))
                .unit(FinancialUnit.TIMES).status(CalculationStatus.SUCCESS).build());
        calcResults.add(FinancialCalculationResult.builder()
                .ratioCode("ROA").ratioName("Tỷ suất sinh lời trên tổng tài sản")
                .category(FinancialRatioCategory.PROFITABILITY).resultValue(new BigDecimal("6.5000"))
                .unit(FinancialUnit.PERCENT).status(CalculationStatus.SUCCESS).build());
        calcResults.add(FinancialCalculationResult.builder()
                .ratioCode("ROE").ratioName("Tỷ suất sinh lời trên vốn CSH")
                .category(FinancialRatioCategory.PROFITABILITY).resultValue(new BigDecimal("15.0000"))
                .unit(FinancialUnit.PERCENT).status(CalculationStatus.SUCCESS).build());

        calculation = FinancialCalculation.builder()
                .id(100L)
                .financialStatement(confirmedStatement)
                .calculationVersion(1)
                .status(CalculationStatus.SUCCESS)
                .results(calcResults)
                .build();

        // Assessment details
        AssessmentDetail ad1 = AssessmentDetail.builder()
                .ratioCodeSnapshot("CURRENT_RATIO")
                .ratioNameSnapshot("Khả năng thanh toán hiện hành")
                .category(FinancialRatioCategory.LIQUIDITY)
                .calculatedValue(new BigDecimal("1.8500"))
                .thresholdValueSnapshot(new BigDecimal("1.5000"))
                .comparisonOperatorSnapshot(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .evaluationStatus(RatioEvaluationStatus.PASS)
                .evaluationExplanation("Đạt ngưỡng an toàn >= 1.5")
                .build();
        AssessmentDetail ad2 = AssessmentDetail.builder()
                .ratioCodeSnapshot("DEBT_TO_EQUITY")
                .ratioNameSnapshot("Hệ số nợ trên vốn CSH")
                .category(FinancialRatioCategory.SOLVENCY)
                .calculatedValue(new BigDecimal("0.6667"))
                .thresholdValueSnapshot(new BigDecimal("1.0000"))
                .comparisonOperatorSnapshot(ComparisonOperator.LESS_THAN_OR_EQUAL)
                .evaluationStatus(RatioEvaluationStatus.PASS)
                .evaluationExplanation("Đòn bẩy tài chính an toàn <= 1.0")
                .build();

        assessment = Assessment.builder()
                .id(200L)
                .financialStatement(confirmedStatement)
                .assessmentVersion(1)
                .details(List.of(ad1, ad2))
                .build();

        // Validation execution
        ValidationResult vr1 = ValidationResult.builder()
                .ruleCodeSnapshot("RULE_BALANCE_SHEET_EQ")
                .ruleNameSnapshot("Cân đối bảng cân đối kế toán")
                .category(ValidationCategory.ACCOUNTING_EQUATION)
                .status(ValidationStatus.PASS)
                .severity(ValidationSeverity.CRITICAL)
                .build();

        validationExecution = ValidationExecution.builder()
                .id(300L)
                .financialStatement(confirmedStatement)
                .executionStatus(ValidationStatus.PASS)
                .totalRules(5)
                .passedRules(5)
                .warningRules(0)
                .failedRules(0)
                .executedAt(LocalDateTime.now())
                .results(List.of(vr1))
                .build();

        // Risk analysis
        IdentifiedRisk ir1 = IdentifiedRisk.builder()
                .id(401L)
                .riskCategory(RiskCategory.LIQUIDITY)
                .riskTitle("Thanh toán nhanh cần theo dõi")
                .riskDescription("Quick ratio gần ngưỡng khuyến cáo")
                .severity(RiskSeverity.LOW)
                .riskStatus(RiskStatus.MONITORING)
                .relatedRatioCode("QUICK_RATIO")
                .actualValue(new BigDecimal("1.2000"))
                .thresholdValue(new BigDecimal("1.0000"))
                .recommendation("Duy trì số dư tiền mặt")
                .potentialImpact("Có thể ảnh hưởng khả năng trả nợ ngắn hạn")
                .build();

        riskAnalysis = RiskAnalysis.builder()
                .id(400L)
                .financialStatement(confirmedStatement)
                .executionStatus(RiskAnalysisStatus.COMPLETED)
                .totalRisks(1)
                .lowRisks(1)
                .mediumRisks(0)
                .highRisks(0)
                .criticalRisks(0)
                .identifiedRisks(List.of(ir1))
                .build();

        // AI analysis
        aiAnalysis = AIAnalysis.builder()
                .id(500L)
                .assessment(assessment)
                .executiveSummary("Doanh nghiệp duy trì sức khỏe tài chính tương đối an toàn.")
                .aiGeneratedComment("{\"executiveSummary\":\"Doanh nghiệp duy trì sức khỏe tài chính tương đối an toàn.\"}")
                .finalComment("{\"executiveSummary\":\"Doanh nghiệp duy trì sức khỏe tài chính tương đối an toàn.\", \"edited\": false}")
                .status(AIAnalysisStatus.COMPLETED)
                .reviewStatus(AIReviewStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
    }

    // 1. Dashboard overview with complete data
    @Test
    @DisplayName("Scenario 1: Dashboard overview with complete data aggregates all subsystems")
    void testDashboardOverview_CompleteData() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(company));
        when(statementRepository.findByCompanyIdWithFilters(eq(1L), any(), any(), eq(FinancialStatementStatus.DATA_CONFIRMED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(confirmedStatement)));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(10L))
                .thenReturn(Optional.of(calculation));
        when(assessmentRepository.findTopByFinancialStatementIdOrderByAssessmentVersionDesc(10L))
                .thenReturn(Optional.of(assessment));
        when(validationExecutionRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(10L))
                .thenReturn(Optional.of(validationExecution));
        when(riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(10L))
                .thenReturn(Optional.of(riskAnalysis));
        when(aiAnalysisRepository.findTopByAssessmentIdOrderByAnalysisVersionDesc(200L))
                .thenReturn(Optional.of(aiAnalysis));

        DashboardOverviewResponse response = dashboardService.getDashboardOverview(1L, null, null);

        assertThat(response).isNotNull();
        assertThat(response.getContext().getHasCompany()).isTrue();
        assertThat(response.getContext().getCompanyName()).isEqualTo("Công ty Cổ phần ABC");
        assertThat(response.getContext().getDataStatus()).isEqualTo("READY");

        // Core KPIs
        assertThat(response.getCoreKpis()).hasSize(4);
        DashboardKPIResponse currentRatioKpi = response.getCoreKpis().stream()
                .filter(k -> "CURRENT_RATIO".equals(k.getCode())).findFirst().orElseThrow();
        assertThat(currentRatioKpi.getValue()).isEqualByComparingTo("1.8500");
        assertThat(currentRatioKpi.getConfiguredThreshold()).isEqualByComparingTo("1.5000");
        assertThat(currentRatioKpi.getComparisonOperator()).isEqualTo(">=");
        assertThat(currentRatioKpi.getEvaluationStatus()).isEqualTo("PASS");

        // Visual analytics
        assertThat(response.getVisualAnalytics().getCapitalStructure().getTotalAssets())
                .isEqualByComparingTo("10000000000");
        assertThat(response.getVisualAnalytics().getCapitalStructure().getDebtRatio())
                .isEqualByComparingTo("0.4000");

        // Validation
        assertThat(response.getValidation().getOverallStatus()).isEqualTo("PASS");
        assertThat(response.getValidation().getTotalRules()).isEqualTo(5);

        // Risks
        assertThat(response.getRiskOverview().getSummary().getTotalRisks()).isEqualTo(1);
        assertThat(response.getRiskOverview().getIdentifiedRisks()).hasSize(1);

        // AI commentary
        assertThat(response.getAiCommentary().getExecutiveSummary()).contains("sức khỏe tài chính");
        assertThat(response.getAiCommentary().getReviewStatus()).isEqualTo("APPROVED");
    }

    // 2. User has no company
    @Test
    @DisplayName("Scenario 2: Dashboard overview when user has no company returns empty context")
    void testDashboardOverview_UserHasNoCompany() {
        when(companyRepository.findByUserIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        DashboardOverviewResponse response = dashboardService.getDashboardOverview(999L, null, null);

        assertThat(response).isNotNull();
        assertThat(response.getContext().getHasCompany()).isFalse();
        assertThat(response.getContext().getDataStatus()).isEqualTo("NO_COMPANY");
        assertThat(response.getCoreKpis()).isEmpty();
        assertThat(response.getFinancialRatios()).isEmpty();
        assertThat(response.getValidation().getOverallStatus()).isEqualTo("NO_COMPANY");
        assertThat(response.getRiskOverview().getExecutionStatus()).isEqualTo("NO_COMPANY");
        assertThat(response.getAiCommentary().getAiGenerationStatus()).isEqualTo("NO_COMPANY");
    }

    // 3. No financial statement exists
    @Test
    @DisplayName("Scenario 3: Dashboard overview when no financial statement exists returns empty statement state")
    void testDashboardOverview_NoFinancialStatement() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(company));
        when(statementRepository.findByCompanyIdWithFilters(eq(1L), any(), any(), eq(FinancialStatementStatus.DATA_CONFIRMED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(statementRepository.findByCompanyIdWithFilters(eq(1L), any(), any(), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        DashboardOverviewResponse response = dashboardService.getDashboardOverview(1L, null, null);

        assertThat(response).isNotNull();
        assertThat(response.getContext().getHasCompany()).isTrue();
        assertThat(response.getContext().getHasFinancialStatement()).isFalse();
        assertThat(response.getContext().getDataStatus()).isEqualTo("NO_FINANCIAL_STATEMENT");
        assertThat(response.getCoreKpis()).isEmpty();
        assertThat(response.getValidation().getOverallStatus()).isEqualTo("NO_FINANCIAL_STATEMENT");
    }

    // 4. Unconfirmed financial statement must not provide official KPIs
    @Test
    @DisplayName("Scenario 4: Unconfirmed statement does not expose official KPIs or calculations")
    void testDashboardOverview_UnconfirmedStatement_NoOfficialKpis() {
        FinancialStatement reviewingStatement = FinancialStatement.builder()
                .id(20L)
                .company(company)
                .period("2024")
                .processingStatus(FinancialStatementStatus.REVIEWING_DATA)
                .items(Collections.emptyList())
                .build();

        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(company));
        when(statementRepository.findByCompanyIdWithFilters(eq(1L), any(), any(), eq(FinancialStatementStatus.DATA_CONFIRMED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(statementRepository.findByCompanyIdWithFilters(eq(1L), any(), any(), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(reviewingStatement)));
        when(validationExecutionRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(20L))
                .thenReturn(Optional.of(validationExecution));

        DashboardOverviewResponse response = dashboardService.getDashboardOverview(1L, null, null);

        assertThat(response).isNotNull();
        assertThat(response.getContext().getFinancialStatementProcessingStatus()).isEqualTo("REVIEWING_DATA");
        assertThat(response.getDataStatuses().getCalculationStatus()).isEqualTo("STATEMENT_NOT_CONFIRMED");
        assertThat(response.getCoreKpis()).isEmpty();
        assertThat(response.getFinancialRatios()).isEmpty();
        // Validation during review is still accessible
        assertThat(response.getValidation().getOverallStatus()).isEqualTo("PASS");
        assertThat(response.getRiskOverview().getExecutionStatus()).isEqualTo("STATEMENT_NOT_CONFIRMED");
    }

    // 5. Missing calculation results
    @Test
    @DisplayName("Scenario 5: Missing calculation results returns CALCULATION_NOT_AVAILABLE with null numeric values")
    void testDashboardOverview_MissingCalculationResults() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(company));
        when(statementRepository.findByCompanyIdWithFilters(eq(1L), any(), any(), eq(FinancialStatementStatus.DATA_CONFIRMED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(confirmedStatement)));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(10L))
                .thenReturn(Optional.empty());
        when(assessmentRepository.findTopByFinancialStatementIdOrderByAssessmentVersionDesc(10L))
                .thenReturn(Optional.empty());
        when(validationExecutionRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(10L))
                .thenReturn(Optional.empty());
        when(riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(10L))
                .thenReturn(Optional.empty());

        DashboardOverviewResponse response = dashboardService.getDashboardOverview(1L, null, null);

        assertThat(response.getDataStatuses().getCalculationStatus()).isEqualTo("CALCULATION_NOT_AVAILABLE");
        assertThat(response.getCoreKpis()).hasSize(4);
        for (DashboardKPIResponse kpi : response.getCoreKpis()) {
            assertThat(kpi.getValue()).isNull();
            assertThat(kpi.getCalculationStatus()).isEqualTo("CALCULATION_NOT_AVAILABLE");
        }
    }

    // 6. Missing validation results
    @Test
    @DisplayName("Scenario 6: Missing validation returns VALIDATION_NOT_AVAILABLE")
    void testDashboardOverview_MissingValidationResults() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(company));
        when(statementRepository.findByCompanyIdWithFilters(eq(1L), any(), any(), eq(FinancialStatementStatus.DATA_CONFIRMED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(confirmedStatement)));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(10L))
                .thenReturn(Optional.of(calculation));
        when(assessmentRepository.findTopByFinancialStatementIdOrderByAssessmentVersionDesc(10L))
                .thenReturn(Optional.of(assessment));
        when(validationExecutionRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(10L))
                .thenReturn(Optional.empty());
        when(riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(10L))
                .thenReturn(Optional.of(riskAnalysis));
        when(aiAnalysisRepository.findTopByAssessmentIdOrderByAnalysisVersionDesc(200L))
                .thenReturn(Optional.of(aiAnalysis));

        DashboardOverviewResponse response = dashboardService.getDashboardOverview(1L, null, null);

        assertThat(response.getValidation().getOverallStatus()).isEqualTo("VALIDATION_NOT_AVAILABLE");
        assertThat(response.getValidation().getTotalRules()).isZero();
    }

    // 7. Missing risk analysis
    @Test
    @DisplayName("Scenario 7: Missing risk analysis returns RISK_ANALYSIS_NOT_AVAILABLE")
    void testDashboardOverview_MissingRiskAnalysis() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(company));
        when(statementRepository.findByCompanyIdWithFilters(eq(1L), any(), any(), eq(FinancialStatementStatus.DATA_CONFIRMED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(confirmedStatement)));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(10L))
                .thenReturn(Optional.of(calculation));
        when(assessmentRepository.findTopByFinancialStatementIdOrderByAssessmentVersionDesc(10L))
                .thenReturn(Optional.of(assessment));
        when(validationExecutionRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(10L))
                .thenReturn(Optional.of(validationExecution));
        when(riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(10L))
                .thenReturn(Optional.empty());
        when(aiAnalysisRepository.findTopByAssessmentIdOrderByAnalysisVersionDesc(200L))
                .thenReturn(Optional.of(aiAnalysis));

        DashboardOverviewResponse response = dashboardService.getDashboardOverview(1L, null, null);

        assertThat(response.getRiskOverview().getExecutionStatus()).isEqualTo("RISK_ANALYSIS_NOT_AVAILABLE");
        assertThat(response.getRiskOverview().getIdentifiedRisks()).isEmpty();
    }

    // 8. Missing AI analysis
    @Test
    @DisplayName("Scenario 8: Missing AI analysis returns AI_ANALYSIS_NOT_AVAILABLE")
    void testDashboardOverview_MissingAIAnalysis() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(company));
        when(statementRepository.findByCompanyIdWithFilters(eq(1L), any(), any(), eq(FinancialStatementStatus.DATA_CONFIRMED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(confirmedStatement)));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(10L))
                .thenReturn(Optional.of(calculation));
        when(assessmentRepository.findTopByFinancialStatementIdOrderByAssessmentVersionDesc(10L))
                .thenReturn(Optional.of(assessment));
        when(validationExecutionRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(10L))
                .thenReturn(Optional.of(validationExecution));
        when(riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(10L))
                .thenReturn(Optional.of(riskAnalysis));
        when(aiAnalysisRepository.findTopByAssessmentIdOrderByAnalysisVersionDesc(200L))
                .thenReturn(Optional.empty());

        DashboardOverviewResponse response = dashboardService.getDashboardOverview(1L, null, null);

        assertThat(response.getAiCommentary().getAiGenerationStatus()).isEqualTo("AI_ANALYSIS_NOT_AVAILABLE");
        assertThat(response.getAiCommentary().getReviewStatus()).isEqualTo("NOT_AVAILABLE");
    }

    // 9. Unconfigured thresholds
    @Test
    @DisplayName("Scenario 9: Unconfigured thresholds return NOT_CONFIGURED without fabricating values")
    void testDashboardOverview_UnconfiguredThresholds() {
        // Assessment with no configured criteria
        Assessment emptyAssessment = Assessment.builder()
                .id(201L)
                .financialStatement(confirmedStatement)
                .details(Collections.emptyList())
                .build();

        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(company));
        when(statementRepository.findByCompanyIdWithFilters(eq(1L), any(), any(), eq(FinancialStatementStatus.DATA_CONFIRMED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(confirmedStatement)));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(10L))
                .thenReturn(Optional.of(calculation));
        when(assessmentRepository.findTopByFinancialStatementIdOrderByAssessmentVersionDesc(10L))
                .thenReturn(Optional.of(emptyAssessment));
        when(validationExecutionRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(10L))
                .thenReturn(Optional.of(validationExecution));
        when(riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(10L))
                .thenReturn(Optional.of(riskAnalysis));
        when(evaluationCriteriaRepository.findByRatioDefinitionRatioCodeAndIsActiveTrue(any()))
                .thenReturn(Collections.emptyList());

        DashboardOverviewResponse response = dashboardService.getDashboardOverview(1L, null, null);

        DashboardKPIResponse roaKpi = response.getCoreKpis().stream()
                .filter(k -> "ROA".equals(k.getCode())).findFirst().orElseThrow();
        assertThat(roaKpi.getConfiguredThreshold()).isNull();
        assertThat(roaKpi.getComparisonOperator()).isNull();
        assertThat(roaKpi.getEvaluationStatus()).isEqualTo("NOT_CONFIGURED");
    }

    // 10. Financial statement ownership validation
    @Test
    @DisplayName("Scenario 10: Ownership validation throws StatementNotFoundException when statement belongs to another company")
    void testResolveStatement_OwnershipValidation_ThrowsWhenBelongsToAnotherCompany() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.getDashboardOverview(1L, null, 999L))
                .isInstanceOf(StatementNotFoundException.class);
    }

    // 11. Historical trend retrieval
    @Test
    @DisplayName("Scenario 11: Historical trends retrieves data points across confirmed reporting periods")
    void testFinancialTrends_Retrieval() {
        FinancialStatement stmt2023 = FinancialStatement.builder()
                .id(9L)
                .company(company)
                .period("2023")
                .fiscalYear(2023)
                .processingStatus(FinancialStatementStatus.DATA_CONFIRMED)
                .build();

        FinancialCalculation calc2023 = FinancialCalculation.builder()
                .id(90L)
                .results(List.of(FinancialCalculationResult.builder()
                        .ratioCode("CURRENT_RATIO")
                        .resultValue(new BigDecimal("1.7000"))
                        .unit(FinancialUnit.TIMES)
                        .status(CalculationStatus.SUCCESS)
                        .build()))
                .build();

        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(company));
        when(statementRepository.findByCompanyIdWithFilters(eq(1L), any(), any(), eq(FinancialStatementStatus.DATA_CONFIRMED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(stmt2023, confirmedStatement)));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(9L))
                .thenReturn(Optional.of(calc2023));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(10L))
                .thenReturn(Optional.of(calculation));

        DashboardTrendResponse trends = dashboardService.getFinancialTrends(1L, "2023", "2024", List.of("CURRENT_RATIO"));

        assertThat(trends).isNotNull();
        assertThat(trends.getAvailablePeriods()).containsExactly("2023", "2024");
        assertThat(trends.getTrendData()).hasSize(2);
        assertThat(trends.getTrendData().get(0).getValue()).isEqualByComparingTo("1.7000");
        assertThat(trends.getTrendData().get(1).getValue()).isEqualByComparingTo("1.8500");
    }

    // 12. Filtering risks by severity, category, and status
    @Test
    @DisplayName("Scenario 12: Filtering risks by severity, category, and status returns matching subsets")
    void testRiskOverview_FilterBySeverityCategoryStatus() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(company));
        when(statementRepository.findByCompanyIdWithFilters(eq(1L), any(), any(), eq(FinancialStatementStatus.DATA_CONFIRMED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(confirmedStatement)));
        when(riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(10L))
                .thenReturn(Optional.of(riskAnalysis));

        // Matching filter
        DashboardRiskResponse filteredMatch = dashboardService.getRiskOverview(
                1L, null, null, RiskSeverity.LOW, RiskCategory.LIQUIDITY, RiskStatus.MONITORING
        );
        assertThat(filteredMatch.getIdentifiedRisks()).hasSize(1);

        // Non-matching severity filter
        DashboardRiskResponse filteredNonMatch = dashboardService.getRiskOverview(
                1L, null, null, RiskSeverity.CRITICAL, null, null
        );
        assertThat(filteredNonMatch.getIdentifiedRisks()).isEmpty();
    }
}
