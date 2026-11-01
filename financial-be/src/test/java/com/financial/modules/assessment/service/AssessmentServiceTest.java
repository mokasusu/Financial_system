package com.financial.modules.assessment.service;

import com.financial.common.exception.*;
import com.financial.modules.assessment.dto.*;
import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.assessment.entity.AssessmentCategoryScore;
import com.financial.modules.assessment.entity.AssessmentDetail;
import com.financial.modules.assessment.enums.AssessmentScoringStatus;
import com.financial.modules.assessment.enums.AssessmentStatus;
import com.financial.modules.assessment.enums.DataQualityStatus;
import com.financial.modules.assessment.enums.HealthClassification;
import com.financial.modules.assessment.mapper.AssessmentMapper;
import com.financial.modules.assessment.repository.AssessmentCategoryScoreRepository;
import com.financial.modules.assessment.repository.AssessmentDetailRepository;
import com.financial.modules.assessment.repository.AssessmentRepository;
import com.financial.modules.assessment.service.impl.AssessmentServiceImpl;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.calculationengine.repository.FinancialCalculationRepository;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.repository.FinancialStatementRepository;
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
class AssessmentServiceTest {

    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private AssessmentDetailRepository detailRepository;
    @Mock
    private AssessmentCategoryScoreRepository categoryScoreRepository;
    @Mock
    private FinancialStatementRepository statementRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private FinancialCalculationRepository calculationRepository;
    @Mock
    private ValidationExecutionRepository validationRepository;
    @Mock
    private RatioEvaluationService ratioEvaluationService;
    @Mock
    private CategoryAggregationService categoryAggregationService;
    @Mock
    private AssessmentScoringService scoringService;

    private final AssessmentMapper mapper = new AssessmentMapper();
    private AssessmentService assessmentService;

    private final Long userId = 1L;
    private final Long statementId = 100L;
    private Company company;
    private FinancialStatement confirmedStatement;
    private FinancialCalculation validCalculation;
    private ValidationExecution validValidation;

    @BeforeEach
    void setUp() {
        assessmentService = new AssessmentServiceImpl(
                assessmentRepository,
                detailRepository,
                categoryScoreRepository,
                statementRepository,
                companyRepository,
                calculationRepository,
                validationRepository,
                ratioEvaluationService,
                categoryAggregationService,
                scoringService,
                mapper
        );

        company = Company.builder().id(10L).companyName("Công ty ABC").build();

        confirmedStatement = FinancialStatement.builder()
                .id(statementId)
                .company(company)
                .fileName("BCTC_2024.xlsx")
                .fiscalYear(2024)
                .period("YEAR")
                .accountingStandard("TT200")
                .processingStatus(FinancialStatementStatus.DATA_CONFIRMED)
                .build();

        validCalculation = FinancialCalculation.builder()
                .id(200L)
                .financialStatement(confirmedStatement)
                .calculationVersion(1)
                .confirmedDataVersion("CONF_V1")
                .status(CalculationStatus.SUCCESS)
                .results(List.of(FinancialCalculationResult.builder()
                        .ratioCode("CURRENT_RATIO")
                        .resultValue(new BigDecimal("1.8000"))
                        .status(CalculationStatus.SUCCESS)
                        .build()))
                .build();

        validValidation = ValidationExecution.builder()
                .id(300L)
                .financialStatement(confirmedStatement)
                .confirmedDataVersion("CONF_V1")
                .executionStatus(ValidationStatus.PASS)
                .totalRules(10)
                .passedRules(10)
                .warningRules(0)
                .failedRules(0)
                .reviewRequiredRules(0)
                .errorRules(0)
                .executedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Create assessment with valid calculation results persists new assessment and returns response")
    void testCreateAssessment_Success() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(confirmedStatement));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId)).thenReturn(Optional.of(validCalculation));
        when(validationRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(statementId)).thenReturn(Optional.of(validValidation));

        List<AssessmentDetail> details = List.of(AssessmentDetail.builder()
                .ratioCodeSnapshot("CURRENT_RATIO")
                .ratioNameSnapshot("Current Ratio")
                .category(FinancialRatioCategory.LIQUIDITY)
                .unit(FinancialUnit.TIMES)
                .calculatedValue(new BigDecimal("1.8000"))
                .evaluationStatus(com.financial.modules.assessment.enums.RatioEvaluationStatus.PASS)
                .build());

        List<AssessmentCategoryScore> categoryScores = List.of(AssessmentCategoryScore.builder()
                .category(FinancialRatioCategory.LIQUIDITY)
                .categoryName("Khả năng thanh toán")
                .totalRatios(1)
                .passedRatios(1)
                .scoringStatus(AssessmentScoringStatus.NOT_CONFIGURED)
                .build());

        when(ratioEvaluationService.evaluateRatios(confirmedStatement, validCalculation)).thenReturn(details);
        when(categoryAggregationService.aggregateCategories(details)).thenReturn(categoryScores);
        when(scoringService.evaluateScoring(details, categoryScores)).thenReturn(ScoringResult.notConfigured());
        when(assessmentRepository.countByFinancialStatementId(statementId)).thenReturn(0);

        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(invocation -> {
            Assessment a = invocation.getArgument(0);
            a.setId(501L);
            return a;
        });

        AssessmentResponse response = assessmentService.createAssessment(userId, statementId, null);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(501L);
        assertThat(response.getAssessmentVersion()).isEqualTo(1);
        assertThat(response.getAssessmentStatus()).isEqualTo(AssessmentStatus.COMPLETED);
        assertThat(response.getScoringStatus()).isEqualTo(AssessmentScoringStatus.NOT_CONFIGURED);
        assertThat(response.getOverallScore()).isNull();
        assertThat(response.getHealthClassification()).isEqualTo(HealthClassification.NOT_CONFIGURED);
        assertThat(response.getDataQualityStatus()).isEqualTo(DataQualityStatus.VALID);
        assertThat(response.getPassedRatios()).isEqualTo(1);

        verify(assessmentRepository).save(any(Assessment.class));
        // Verify source financial statement was not modified
        verify(statementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create assessment rejects unconfirmed draft financial statement")
    void testCreateAssessment_RejectsUnconfirmedStatement() {
        confirmedStatement.setProcessingStatus(FinancialStatementStatus.REVIEWING_DATA);

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(confirmedStatement));

        assertThatThrownBy(() -> assessmentService.createAssessment(userId, statementId, null))
                .isInstanceOf(UnconfirmedStatementException.class);

        verify(assessmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create assessment fails when calculation results are missing")
    void testCreateAssessment_FailsWhenCalculationMissing() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(confirmedStatement));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assessmentService.createAssessment(userId, statementId, null))
                .isInstanceOf(CalculationNotFoundException.class);
    }

    @Test
    @DisplayName("Create assessment fails when calculation execution contains no valid results")
    void testCreateAssessment_FailsWhenCalculationEmpty() {
        validCalculation.setResults(Collections.emptyList());

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(confirmedStatement));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId)).thenReturn(Optional.of(validCalculation));

        assertThatThrownBy(() -> assessmentService.createAssessment(userId, statementId, null))
                .isInstanceOf(InvalidAssessmentStateException.class);
    }

    @Test
    @DisplayName("Users cannot assess another user's financial statement (ownership check)")
    void testOwnershipCheck_ThrowsWhenStatementNotOwned() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assessmentService.createAssessment(userId, statementId, null))
                .isInstanceOf(StatementNotFoundException.class);
    }

    @Test
    @DisplayName("Mismatched calculation and validation confirmed versions flags REVIEW_REQUIRED")
    void testVersionMismatch_FlagsReviewRequired() {
        validCalculation.setConfirmedDataVersion("CONF_V2");
        validValidation.setConfirmedDataVersion("CONF_V1"); // mismatch!

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(confirmedStatement));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId)).thenReturn(Optional.of(validCalculation));
        when(validationRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(statementId)).thenReturn(Optional.of(validValidation));

        when(ratioEvaluationService.evaluateRatios(any(), any())).thenReturn(Collections.emptyList());
        when(categoryAggregationService.aggregateCategories(any())).thenReturn(Collections.emptyList());
        when(scoringService.evaluateScoring(any(), any())).thenReturn(ScoringResult.notConfigured());

        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(invocation -> {
            Assessment a = invocation.getArgument(0);
            a.setId(502L);
            return a;
        });

        AssessmentResponse response = assessmentService.createAssessment(userId, statementId, null);

        assertThat(response.getDataQualityStatus()).isEqualTo(DataQualityStatus.REVIEW_REQUIRED);
    }

    @Test
    @DisplayName("Assessment without validation execution returns DataQualityStatus.NOT_VALIDATED")
    void testAssessmentWithoutValidation_SetsNotValidated() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(confirmedStatement));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId)).thenReturn(Optional.of(validCalculation));
        when(validationRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(statementId)).thenReturn(Optional.empty());

        when(ratioEvaluationService.evaluateRatios(any(), any())).thenReturn(Collections.emptyList());
        when(categoryAggregationService.aggregateCategories(any())).thenReturn(Collections.emptyList());
        when(scoringService.evaluateScoring(any(), any())).thenReturn(ScoringResult.notConfigured());

        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(invocation -> {
            Assessment a = invocation.getArgument(0);
            a.setId(503L);
            return a;
        });

        AssessmentResponse response = assessmentService.createAssessment(userId, statementId, null);

        assertThat(response.getDataQualityStatus()).isEqualTo(DataQualityStatus.NOT_VALIDATED);
        assertThat(response.getValidationExecutionId()).isNull();
    }

    @Test
    @DisplayName("New assessment increments version and preserves historical records")
    void testAssessmentVersionIncrement() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(confirmedStatement));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId)).thenReturn(Optional.of(validCalculation));
        when(validationRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(statementId)).thenReturn(Optional.of(validValidation));

        when(ratioEvaluationService.evaluateRatios(any(), any())).thenReturn(Collections.emptyList());
        when(categoryAggregationService.aggregateCategories(any())).thenReturn(Collections.emptyList());
        when(scoringService.evaluateScoring(any(), any())).thenReturn(ScoringResult.notConfigured());

        // Previous 2 assessments exist
        when(assessmentRepository.countByFinancialStatementId(statementId)).thenReturn(2);

        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(invocation -> {
            Assessment a = invocation.getArgument(0);
            a.setId(504L);
            return a;
        });

        AssessmentResponse response = assessmentService.createAssessment(userId, statementId, null);

        assertThat(response.getAssessmentVersion()).isEqualTo(3);
    }

    @Test
    @DisplayName("getLatestAssessment returns assessment when found")
    void testGetLatestAssessment_Success() {
        Assessment assessment = Assessment.builder()
                .id(1L)
                .assessmentVersion(1)
                .assessmentStatus(AssessmentStatus.COMPLETED)
                .scoringStatus(AssessmentScoringStatus.NOT_CONFIGURED)
                .healthClassification(HealthClassification.NOT_CONFIGURED)
                .dataQualityStatus(DataQualityStatus.VALID)
                .financialStatement(confirmedStatement)
                .company(company)
                .build();

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(confirmedStatement));
        when(assessmentRepository.findTopByFinancialStatementIdOrderByAssessmentVersionDesc(statementId))
                .thenReturn(Optional.of(assessment));

        AssessmentResponse response = assessmentService.getLatestAssessment(userId, statementId);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getLatestAssessment throws AssessmentNotFoundException when no assessment exists")
    void testGetLatestAssessment_ThrowsWhenNone() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, company.getId())).thenReturn(Optional.of(confirmedStatement));
        when(assessmentRepository.findTopByFinancialStatementIdOrderByAssessmentVersionDesc(statementId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> assessmentService.getLatestAssessment(userId, statementId))
                .isInstanceOf(AssessmentNotFoundException.class);
    }
}

