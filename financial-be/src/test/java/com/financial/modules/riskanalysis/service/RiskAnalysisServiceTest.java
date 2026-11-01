package com.financial.modules.riskanalysis.service;

import com.financial.common.exception.*;
import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.assessment.enums.AssessmentStatus;
import com.financial.modules.assessment.repository.AssessmentRepository;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.calculationengine.repository.FinancialCalculationRepository;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.repository.FinancialStatementRepository;
import com.financial.modules.riskanalysis.dto.*;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskAnalysis;
import com.financial.modules.riskanalysis.entity.RiskRule;
import com.financial.modules.riskanalysis.enums.RiskAnalysisStatus;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import com.financial.modules.riskanalysis.mapper.RiskAnalysisMapper;
import com.financial.modules.riskanalysis.repository.IdentifiedRiskRepository;
import com.financial.modules.riskanalysis.repository.RiskAnalysisRepository;
import com.financial.modules.riskanalysis.repository.RiskRuleRepository;
import com.financial.modules.riskanalysis.service.impl.RiskAnalysisServiceImpl;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.enums.ValidationStatus;
import com.financial.modules.validation.repository.ValidationExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskAnalysisServiceTest {

    @Mock
    private RiskAnalysisRepository riskAnalysisRepository;
    @Mock
    private IdentifiedRiskRepository identifiedRiskRepository;
    @Mock
    private RiskRuleRepository riskRuleRepository;
    @Mock
    private FinancialStatementRepository statementRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private FinancialCalculationRepository calculationRepository;
    @Mock
    private ValidationExecutionRepository validationRepository;
    @Mock
    private RiskDetectionService riskDetectionService;
    @Mock
    private RiskAggregationService riskAggregationService;

    private final RiskAnalysisMapper mapper = new RiskAnalysisMapper();
    private RiskAnalysisService riskAnalysisService;

    private final Long userId = 1L;
    private final Long statementId = 100L;
    private Company company;
    private FinancialStatement statement;
    private FinancialCalculation calculation;
    private Assessment assessment;
    private ValidationExecution validation;
    private RiskRule sampleRule;

    @BeforeEach
    void setUp() {
        riskAnalysisService = new RiskAnalysisServiceImpl(
                riskAnalysisRepository,
                identifiedRiskRepository,
                riskRuleRepository,
                statementRepository,
                companyRepository,
                assessmentRepository,
                calculationRepository,
                validationRepository,
                riskDetectionService,
                riskAggregationService,
                mapper
        );

        company = Company.builder().id(10L).companyName("Công ty ABC").build();

        statement = FinancialStatement.builder()
                .id(statementId)
                .company(company)
                .processingStatus(FinancialStatementStatus.DATA_CONFIRMED)
                .build();

        calculation = FinancialCalculation.builder()
                .id(200L)
                .financialStatement(statement)
                .calculationVersion(1)
                .confirmedDataVersion("v1")
                .status(CalculationStatus.SUCCESS)
                .results(new ArrayList<>())
                .build();

        calculation.getResults().add(FinancialCalculationResult.builder()
                .ratioCode("CURRENT_RATIO")
                .ratioName("Current Ratio")
                .category(FinancialRatioCategory.LIQUIDITY)
                .unit(FinancialUnit.TIMES)
                .resultValue(new BigDecimal("0.8500"))
                .status(CalculationStatus.SUCCESS)
                .build());

        assessment = Assessment.builder()
                .id(300L)
                .financialStatement(statement)
                .financialCalculation(calculation)
                .assessmentVersion(1)
                .confirmedDataVersion("v1")
                .assessmentStatus(AssessmentStatus.COMPLETED)
                .build();

        validation = ValidationExecution.builder()
                .id(400L)
                .financialStatement(statement)
                .confirmedDataVersion("v1")
                .executionStatus(ValidationStatus.PASS)
                .build();

        sampleRule = RiskRule.builder()
                .id(1L)
                .ruleCode("RULE_CR_LOW")
                .ruleName("Khả năng thanh toán hiện hành thấp")
                .riskCategory(RiskCategory.LIQUIDITY)
                .ratioCode("CURRENT_RATIO")
                .comparisonOperator(ComparisonOperator.LESS_THAN)
                .thresholdValue(new BigDecimal("1.0000"))
                .severity(RiskSeverity.HIGH)
                .explanationTemplate("Chỉ số {ratioCode} thấp: {actualValue}")
                .recommendationTemplate("Cần bổ sung nguồn vốn lưu động")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("analyzeRisks succeeds and persists risk analysis with identified risks")
    void testAnalyzeRisks_Success() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(statement));
        when(assessmentRepository.findTopByFinancialStatementIdOrderByAssessmentVersionDesc(statementId)).thenReturn(Optional.of(assessment));
        when(validationRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(statementId)).thenReturn(Optional.of(validation));
        when(riskRuleRepository.findByIsActiveTrue()).thenReturn(List.of(sampleRule));
        when(riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(statementId)).thenReturn(Optional.empty());
        when(riskAnalysisRepository.countByFinancialStatementId(statementId)).thenReturn(0);

        IdentifiedRisk detectedRisk = IdentifiedRisk.builder()
                .riskCategory(RiskCategory.LIQUIDITY)
                .severity(RiskSeverity.HIGH)
                .riskStatus(RiskStatus.DETECTED)
                .ruleCodeSnapshot("RULE_CR_LOW")
                .riskTitle("Khả năng thanh toán hiện hành thấp")
                .riskDescription("Chỉ số CURRENT_RATIO thấp: 0.8500")
                .recommendation("Cần bổ sung nguồn vốn lưu động")
                .detectedAt(LocalDateTime.now())
                .build();

        when(riskDetectionService.detectAllRisks(any(), any())).thenReturn(List.of(detectedRisk));
        when(riskAnalysisRepository.save(any())).thenAnswer(invocation -> {
            RiskAnalysis toSave = invocation.getArgument(0);
            toSave.setId(501L);
            return toSave;
        });

        RiskAnalysisRequest request = RiskAnalysisRequest.builder().analysisNotes("Test notes").build();
        RiskAnalysisResponse response = riskAnalysisService.analyzeRisks(userId, statementId, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(501L);
        assertThat(response.getAnalysisVersion()).isEqualTo(1);
        assertThat(response.getExecutionStatus()).isEqualTo(RiskAnalysisStatus.COMPLETED);
        assertThat(response.getTotalRisks()).isEqualTo(1);
        assertThat(response.getHighRisks()).isEqualTo(1);
        assertThat(response.getIdentifiedRisks()).hasSize(1);
        assertThat(response.getIdentifiedRisks().get(0).getSeverity()).isEqualTo(RiskSeverity.HIGH);
        assertThat(response.getIdentifiedRisks().get(0).getRiskStatus()).isEqualTo(RiskStatus.DETECTED);
    }

    @Test
    @DisplayName("analyzeRisks throws UnconfirmedStatementException when statement is in REVIEWING_DATA status")
    void testAnalyzeRisks_UnconfirmedStatement() {
        statement.setProcessingStatus(FinancialStatementStatus.REVIEWING_DATA);
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(statement));

        assertThatThrownBy(() -> riskAnalysisService.analyzeRisks(userId, statementId, null))
                .isInstanceOf(UnconfirmedStatementException.class);
    }

    @Test
    @DisplayName("analyzeRisks throws StatementNotFoundException when company does not exist")
    void testAnalyzeRisks_CompanyNotFound() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riskAnalysisService.analyzeRisks(userId, statementId, null))
                .isInstanceOf(StatementNotFoundException.class);
    }

    @Test
    @DisplayName("analyzeRisks throws StatementNotFoundException when statement does not exist")
    void testAnalyzeRisks_StatementNotFound() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riskAnalysisService.analyzeRisks(userId, statementId, null))
                .isInstanceOf(StatementNotFoundException.class);
    }

    @Test
    @DisplayName("analyzeRisks throws InvalidRiskAnalysisStateException on calculation and assessment version mismatch")
    void testAnalyzeRisks_VersionMismatch_CalcAndAssessment() {
        calculation.setConfirmedDataVersion("v1");
        assessment.setConfirmedDataVersion("v2");

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(statement));
        when(assessmentRepository.findTopByFinancialStatementIdOrderByAssessmentVersionDesc(statementId)).thenReturn(Optional.of(assessment));
        when(validationRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(statementId)).thenReturn(Optional.of(validation));

        assertThatThrownBy(() -> riskAnalysisService.analyzeRisks(userId, statementId, null))
                .isInstanceOf(InvalidRiskAnalysisStateException.class)
                .hasMessageContaining("Confirmed data version mismatch between calculation");
    }

    @Test
    @DisplayName("analyzeRisks throws InvalidRiskAnalysisStateException on calculation and validation version mismatch")
    void testAnalyzeRisks_VersionMismatch_CalcAndValidation() {
        calculation.setConfirmedDataVersion("v1");
        assessment.setConfirmedDataVersion("v1");
        validation.setConfirmedDataVersion("v2");

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(statement));
        when(assessmentRepository.findTopByFinancialStatementIdOrderByAssessmentVersionDesc(statementId)).thenReturn(Optional.of(assessment));
        when(validationRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(statementId)).thenReturn(Optional.of(validation));

        assertThatThrownBy(() -> riskAnalysisService.analyzeRisks(userId, statementId, null))
                .isInstanceOf(InvalidRiskAnalysisStateException.class)
                .hasMessageContaining("Confirmed data version mismatch between calculation ('v1') and validation ('v2')");
    }

    @Test
    @DisplayName("analyzeRisks throws InvalidRiskAnalysisStateException when calculation has no results")
    void testAnalyzeRisks_CalculationWithoutResults() {
        calculation.getResults().clear();

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(statement));
        when(assessmentRepository.findTopByFinancialStatementIdOrderByAssessmentVersionDesc(statementId)).thenReturn(Optional.of(assessment));

        assertThatThrownBy(() -> riskAnalysisService.analyzeRisks(userId, statementId, null))
                .isInstanceOf(InvalidRiskAnalysisStateException.class)
                .hasMessageContaining("contains no valid ratio results");
    }

    @Test
    @DisplayName("getLatestRiskAnalysis returns latest analysis response")
    void testGetLatestRiskAnalysis_Success() {
        RiskAnalysis analysis = RiskAnalysis.builder()
                .id(501L)
                .financialStatement(statement)
                .company(company)
                .analysisVersion(1)
                .executionStatus(RiskAnalysisStatus.COMPLETED)
                .analyzedAt(LocalDateTime.now())
                .identifiedRisks(new ArrayList<>())
                .build();

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(statement));
        when(riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(statementId)).thenReturn(Optional.of(analysis));

        RiskAnalysisResponse response = riskAnalysisService.getLatestRiskAnalysis(userId, statementId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(501L);
        assertThat(response.getAnalysisVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("getLatestRiskAnalysis throws RiskAnalysisNotFoundException when no analysis exists")
    void testGetLatestRiskAnalysis_NotFound() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(statement));
        when(riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(statementId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riskAnalysisService.getLatestRiskAnalysis(userId, statementId))
                .isInstanceOf(RiskAnalysisNotFoundException.class);
    }

    @Test
    @DisplayName("getRiskAnalysisById returns analysis when exists")
    void testGetRiskAnalysisById_Success() {
        RiskAnalysis analysis = RiskAnalysis.builder()
                .id(501L)
                .financialStatement(statement)
                .company(company)
                .analysisVersion(1)
                .executionStatus(RiskAnalysisStatus.COMPLETED)
                .analyzedAt(LocalDateTime.now())
                .identifiedRisks(new ArrayList<>())
                .build();

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(statement));
        when(riskAnalysisRepository.findByIdAndFinancialStatementId(501L, statementId)).thenReturn(Optional.of(analysis));

        RiskAnalysisResponse response = riskAnalysisService.getRiskAnalysisById(userId, statementId, 501L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(501L);
    }

    @Test
    @DisplayName("getRiskAnalysisHistory returns paginated history")
    void testGetRiskAnalysisHistory() {
        RiskAnalysis analysis = RiskAnalysis.builder()
                .id(501L)
                .financialStatement(statement)
                .company(company)
                .analysisVersion(1)
                .executionStatus(RiskAnalysisStatus.COMPLETED)
                .analyzedAt(LocalDateTime.now())
                .totalRisks(2)
                .highRisks(1)
                .mediumRisks(1)
                .build();

        Page<RiskAnalysis> page = new PageImpl<>(List.of(analysis));
        Pageable pageable = PageRequest.of(0, 10);

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(statement));
        when(riskAnalysisRepository.findByFinancialStatementIdOrderByAnalysisVersionDesc(statementId, pageable)).thenReturn(page);

        Page<RiskAnalysisHistoryResponse> result = riskAnalysisService.getRiskAnalysisHistory(userId, statementId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(501L);
        assertThat(result.getContent().get(0).getTotalRisks()).isEqualTo(2);
    }

    @Test
    @DisplayName("getIdentifiedRisks returns filtered list of identified risks")
    void testGetIdentifiedRisks() {
        IdentifiedRisk risk = IdentifiedRisk.builder()
                .id(1L)
                .riskCategory(RiskCategory.LIQUIDITY)
                .severity(RiskSeverity.HIGH)
                .riskStatus(RiskStatus.DETECTED)
                .riskTitle("Rủi ro thanh khoản")
                .detectedAt(LocalDateTime.now())
                .build();

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(statement));
        when(riskAnalysisRepository.existsById(501L)).thenReturn(true);
        when(identifiedRiskRepository.findAll(any(Specification.class))).thenReturn(List.of(risk));

        List<IdentifiedRiskResponse> risks = riskAnalysisService.getIdentifiedRisks(
                userId, statementId, 501L, RiskCategory.LIQUIDITY, RiskSeverity.HIGH, RiskStatus.DETECTED
        );

        assertThat(risks).hasSize(1);
        assertThat(risks.get(0).getRiskCategory()).isEqualTo(RiskCategory.LIQUIDITY);
        assertThat(risks.get(0).getSeverity()).isEqualTo(RiskSeverity.HIGH);
    }

    @Test
    @DisplayName("getRiskSummary delegates to aggregation service")
    void testGetRiskSummary() {
        RiskAnalysis analysis = RiskAnalysis.builder().id(501L).build();
        RiskSummaryResponse mockSummary = RiskSummaryResponse.builder().riskAnalysisId(501L).totalRisks(3).build();

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(statement));
        when(riskAnalysisRepository.findByIdAndFinancialStatementId(501L, statementId)).thenReturn(Optional.of(analysis));
        when(riskAggregationService.aggregateRiskSummary(analysis)).thenReturn(mockSummary);

        RiskSummaryResponse summary = riskAnalysisService.getRiskSummary(userId, statementId, 501L);

        assertThat(summary).isNotNull();
        assertThat(summary.getRiskAnalysisId()).isEqualTo(501L);
        assertThat(summary.getTotalRisks()).isEqualTo(3);
    }

    // Risk Rule CRUD tests

    @Test
    @DisplayName("createRiskRule creates and saves rule when code is unique")
    void testCreateRiskRule_Success() {
        RiskRuleRequest request = RiskRuleRequest.builder()
                .ruleCode("RULE_QUICK_LOW")
                .ruleName("Khả năng thanh toán nhanh thấp")
                .riskCategory(RiskCategory.LIQUIDITY)
                .ratioCode("QUICK_RATIO")
                .comparisonOperator(ComparisonOperator.LESS_THAN)
                .thresholdValue(new BigDecimal("0.8000"))
                .severity(RiskSeverity.HIGH)
                .explanationTemplate("Quick ratio thấp: {actualValue}")
                .recommendationTemplate("Cần nâng cao tài sản có tính thanh khoản cao")
                .isActive(true)
                .build();

        when(riskRuleRepository.existsByRuleCode("RULE_QUICK_LOW")).thenReturn(false);
        when(riskRuleRepository.save(any(RiskRule.class))).thenAnswer(invocation -> {
            RiskRule r = invocation.getArgument(0);
            r.setId(2L);
            return r;
        });

        RiskRuleResponse response = riskAnalysisService.createRiskRule(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getRuleCode()).isEqualTo("RULE_QUICK_LOW");
    }

    @Test
    @DisplayName("createRiskRule throws DuplicateRiskRuleException when code already exists")
    void testCreateRiskRule_Duplicate() {
        RiskRuleRequest request = RiskRuleRequest.builder().ruleCode("RULE_CR_LOW").build();
        when(riskRuleRepository.existsByRuleCode("RULE_CR_LOW")).thenReturn(true);

        assertThatThrownBy(() -> riskAnalysisService.createRiskRule(request))
                .isInstanceOf(DuplicateRiskRuleException.class);
    }

    @Test
    @DisplayName("getRiskRuleById returns rule when exists")
    void testGetRiskRuleById_Success() {
        when(riskRuleRepository.findById(1L)).thenReturn(Optional.of(sampleRule));

        RiskRuleResponse response = riskAnalysisService.getRiskRuleById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRuleCode()).isEqualTo("RULE_CR_LOW");
    }

    @Test
    @DisplayName("getRiskRuleById throws RiskRuleNotFoundException when not found")
    void testGetRiskRuleById_NotFound() {
        when(riskRuleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riskAnalysisService.getRiskRuleById(99L))
                .isInstanceOf(RiskRuleNotFoundException.class);
    }

    @Test
    @DisplayName("updateRiskRule updates rule and returns response")
    void testUpdateRiskRule_Success() {
        RiskRuleRequest request = RiskRuleRequest.builder()
                .ruleCode("RULE_CR_LOW")
                .ruleName("Khả năng thanh toán hiện hành rất thấp")
                .riskCategory(RiskCategory.LIQUIDITY)
                .ratioCode("CURRENT_RATIO")
                .comparisonOperator(ComparisonOperator.LESS_THAN)
                .thresholdValue(new BigDecimal("1.2000"))
                .severity(RiskSeverity.CRITICAL)
                .explanationTemplate("Cảnh báo")
                .recommendationTemplate("Khắc phục ngay")
                .isActive(true)
                .build();

        when(riskRuleRepository.findById(1L)).thenReturn(Optional.of(sampleRule));
        when(riskRuleRepository.existsByRuleCodeAndIdNot("RULE_CR_LOW", 1L)).thenReturn(false);
        when(riskRuleRepository.save(any(RiskRule.class))).thenReturn(sampleRule);

        RiskRuleResponse response = riskAnalysisService.updateRiskRule(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getRuleName()).isEqualTo("Khả năng thanh toán hiện hành rất thấp");
        assertThat(response.getSeverity()).isEqualTo(RiskSeverity.CRITICAL);
    }

    @Test
    @DisplayName("updateRiskRuleStatus updates active flag")
    void testUpdateRiskRuleStatus() {
        when(riskRuleRepository.findById(1L)).thenReturn(Optional.of(sampleRule));
        when(riskRuleRepository.save(any(RiskRule.class))).thenReturn(sampleRule);

        RiskRuleResponse response = riskAnalysisService.updateRiskRuleStatus(1L, false);

        assertThat(response).isNotNull();
        assertThat(response.getIsActive()).isFalse();
    }
}
