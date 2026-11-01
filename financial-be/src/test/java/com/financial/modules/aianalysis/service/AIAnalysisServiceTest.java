package com.financial.modules.aianalysis.service;

import com.financial.common.exception.*;
import com.financial.modules.aianalysis.config.OpenRouterProperties;
import com.financial.modules.aianalysis.dto.AIAnalysisFinalCommentRequest;
import com.financial.modules.aianalysis.dto.AIAnalysisGenerateRequest;
import com.financial.modules.aianalysis.dto.AIAnalysisHistoryResponse;
import com.financial.modules.aianalysis.dto.AIAnalysisResponse;
import com.financial.modules.aianalysis.entity.AIAnalysis;
import com.financial.modules.aianalysis.enums.AIAnalysisStatus;
import com.financial.modules.aianalysis.enums.AIReviewStatus;
import com.financial.modules.aianalysis.mapper.AIAnalysisMapper;
import com.financial.modules.aianalysis.repository.AIAnalysisRepository;
import com.financial.modules.aianalysis.service.impl.AIAnalysisServiceImpl;
import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.assessment.enums.AssessmentStatus;
import com.financial.modules.assessment.repository.AssessmentRepository;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.calculationengine.repository.FinancialCalculationRepository;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.riskanalysis.entity.RiskAnalysis;
import com.financial.modules.riskanalysis.repository.RiskAnalysisRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIAnalysisServiceTest {

    @Mock
    private AIAnalysisRepository aiAnalysisRepository;
    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private FinancialCalculationRepository calculationRepository;
    @Mock
    private ValidationExecutionRepository validationRepository;
    @Mock
    private RiskAnalysisRepository riskAnalysisRepository;
    @Mock
    private AIAnalysisPayloadBuilder payloadBuilder;
    @Mock
    private AIAnalysisAsyncProcessor asyncProcessor;

    private final AIAnalysisMapper mapper = new AIAnalysisMapper();
    private OpenRouterProperties openRouterProperties;
    private AIAnalysisService aiAnalysisService;

    private final Long userId = 1L;
    private final Long assessmentId = 10L;
    private Company company;
    private FinancialStatement statement;
    private FinancialCalculation calculation;
    private Assessment assessment;
    private ValidationExecution validation;
    private RiskAnalysis riskAnalysis;

    @BeforeEach
    void setUp() {
        openRouterProperties = new OpenRouterProperties();
        openRouterProperties.setModel("openrouter/free");

        aiAnalysisService = new AIAnalysisServiceImpl(
                aiAnalysisRepository,
                assessmentRepository,
                companyRepository,
                calculationRepository,
                validationRepository,
                riskAnalysisRepository,
                payloadBuilder,
                asyncProcessor,
                mapper,
                openRouterProperties
        );

        company = Company.builder().id(10L).companyName("Công ty ABC").build();

        statement = FinancialStatement.builder()
                .id(100L)
                .company(company)
                .processingStatus(FinancialStatementStatus.DATA_CONFIRMED)
                .accountingStandard("TT200")
                .period("2025")
                .unit("VND")
                .build();

        FinancialCalculationResult calcResult = FinancialCalculationResult.builder()
                .ratioCode("CURRENT_RATIO")
                .resultValue(new BigDecimal("1.8500"))
                .status(CalculationStatus.SUCCESS)
                .build();

        calculation = FinancialCalculation.builder()
                .id(200L)
                .financialStatement(statement)
                .results(List.of(calcResult))
                .build();

        assessment = Assessment.builder()
                .id(assessmentId)
                .company(company)
                .financialStatement(statement)
                .financialCalculation(calculation)
                .assessmentVersion(1)
                .confirmedDataVersion("v1")
                .assessmentStatus(AssessmentStatus.COMPLETED)
                .build();

        validation = ValidationExecution.builder()
                .id(400L)
                .financialStatement(statement)
                .executionStatus(ValidationStatus.PASS)
                .build();

        riskAnalysis = RiskAnalysis.builder()
                .id(500L)
                .financialStatement(statement)
                .company(company)
                .identifiedRisks(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("generateAIAnalysis initiates background analysis and returns 202 status")
    void testGenerateAIAnalysis_Success() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(statement.getId()))
                .thenReturn(Optional.of(riskAnalysis));
        when(aiAnalysisRepository.findFirstByAssessmentIdAndStatusIn(eq(assessmentId), any()))
                .thenReturn(Optional.empty());
        when(aiAnalysisRepository.countByAssessmentId(assessmentId)).thenReturn(0);
        when(aiAnalysisRepository.save(any(AIAnalysis.class))).thenAnswer(invocation -> {
            AIAnalysis a = invocation.getArgument(0);
            a.setId(1L);
            return a;
        });

        AIAnalysisGenerateRequest request = AIAnalysisGenerateRequest.builder().modelOverride("openrouter/free").build();
        AIAnalysisResponse response = aiAnalysisService.generateAIAnalysis(userId, assessmentId, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(AIAnalysisStatus.PENDING);
        assertThat(response.getReviewStatus()).isEqualTo(AIReviewStatus.PENDING);
        assertThat(response.getAnalysisVersion()).isEqualTo(1);

        verify(asyncProcessor).processAIAnalysisAsync(eq(1L), any(), eq("openrouter/free"));
    }

    @Test
    @DisplayName("generateAIAnalysis throws StatementNotFoundException when company does not exist")
    void testGenerateAIAnalysis_CompanyNotFound() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiAnalysisService.generateAIAnalysis(userId, assessmentId, null))
                .isInstanceOf(StatementNotFoundException.class);
    }

    @Test
    @DisplayName("generateAIAnalysis throws AssessmentNotFoundException when assessment belongs to another company")
    void testGenerateAIAnalysis_AssessmentBelongsToOtherCompany() {
        Company otherCompany = Company.builder().id(99L).build();
        assessment.setCompany(otherCompany);

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));

        assertThatThrownBy(() -> aiAnalysisService.generateAIAnalysis(userId, assessmentId, null))
                .isInstanceOf(AssessmentNotFoundException.class);
    }

    @Test
    @DisplayName("generateAIAnalysis throws UnconfirmedStatementException when statement is not confirmed")
    void testGenerateAIAnalysis_UnconfirmedStatement() {
        statement.setProcessingStatus(FinancialStatementStatus.REVIEWING_DATA);

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));

        assertThatThrownBy(() -> aiAnalysisService.generateAIAnalysis(userId, assessmentId, null))
                .isInstanceOf(UnconfirmedStatementException.class);
    }

    @Test
    @DisplayName("generateAIAnalysis throws UpstreamDataUnavailableException when calculation has no results")
    void testGenerateAIAnalysis_MissingCalculationResults() {
        calculation.setResults(new ArrayList<>());

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));

        assertThatThrownBy(() -> aiAnalysisService.generateAIAnalysis(userId, assessmentId, null))
                .isInstanceOf(UpstreamDataUnavailableException.class)
                .hasMessageContaining("calculation results are missing");
    }

    @Test
    @DisplayName("generateAIAnalysis throws UpstreamDataUnavailableException when risk analysis is missing")
    void testGenerateAIAnalysis_MissingRiskAnalysis() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(statement.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiAnalysisService.generateAIAnalysis(userId, assessmentId, null))
                .isInstanceOf(UpstreamDataUnavailableException.class)
                .hasMessageContaining("Risk analysis results are missing");
    }

    @Test
    @DisplayName("generateAIAnalysis throws ConcurrentAIAnalysisException when analysis is already running")
    void testGenerateAIAnalysis_ConcurrentRunning() {
        AIAnalysis running = AIAnalysis.builder().id(2L).status(AIAnalysisStatus.PROCESSING).build();

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(statement.getId()))
                .thenReturn(Optional.of(riskAnalysis));
        when(aiAnalysisRepository.findFirstByAssessmentIdAndStatusIn(eq(assessmentId), any()))
                .thenReturn(Optional.of(running));

        assertThatThrownBy(() -> aiAnalysisService.generateAIAnalysis(userId, assessmentId, null))
                .isInstanceOf(ConcurrentAIAnalysisException.class);
    }

    @Test
    @DisplayName("getLatestAIAnalysis returns analysis response on success")
    void testGetLatestAIAnalysis_Success() {
        AIAnalysis analysis = AIAnalysis.builder()
                .id(1L)
                .assessment(assessment)
                .financialStatement(statement)
                .company(company)
                .status(AIAnalysisStatus.COMPLETED)
                .reviewStatus(AIReviewStatus.PENDING)
                .build();

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiAnalysisRepository.findTopByAssessmentIdOrderByAnalysisVersionDesc(assessmentId))
                .thenReturn(Optional.of(analysis));

        AIAnalysisResponse response = aiAnalysisService.getLatestAIAnalysis(userId, assessmentId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getLatestAIAnalysis throws AIAnalysisNotFoundException when none exists")
    void testGetLatestAIAnalysis_NotFound() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiAnalysisRepository.findTopByAssessmentIdOrderByAnalysisVersionDesc(assessmentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiAnalysisService.getLatestAIAnalysis(userId, assessmentId))
                .isInstanceOf(AIAnalysisNotFoundException.class);
    }

    @Test
    @DisplayName("getAIAnalysisHistory returns paginated history")
    void testGetAIAnalysisHistory() {
        AIAnalysis analysis = AIAnalysis.builder()
                .id(1L)
                .assessment(assessment)
                .status(AIAnalysisStatus.COMPLETED)
                .reviewStatus(AIReviewStatus.PENDING)
                .build();

        Page<AIAnalysis> page = new PageImpl<>(List.of(analysis));
        Pageable pageable = PageRequest.of(0, 10);

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiAnalysisRepository.findByAssessmentIdOrderByAnalysisVersionDesc(assessmentId, pageable))
                .thenReturn(page);

        Page<AIAnalysisHistoryResponse> result = aiAnalysisService.getAIAnalysisHistory(userId, assessmentId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("updateFinalComment updates finalComment and sets EDITED without changing aiGeneratedComment")
    void testUpdateFinalComment_Success() {
        AIAnalysis analysis = AIAnalysis.builder()
                .id(1L)
                .company(company)
                .assessment(assessment)
                .financialStatement(statement)
                .status(AIAnalysisStatus.COMPLETED)
                .reviewStatus(AIReviewStatus.PENDING)
                .aiGeneratedComment("Original AI commentary")
                .finalComment("Original AI commentary")
                .build();

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(aiAnalysisRepository.findByIdAndCompanyId(1L, company.getId())).thenReturn(Optional.of(analysis));
        when(aiAnalysisRepository.save(any(AIAnalysis.class))).thenReturn(analysis);

        AIAnalysisFinalCommentRequest req = AIAnalysisFinalCommentRequest.builder()
                .finalComment("User edited commentary")
                .build();

        AIAnalysisResponse response = aiAnalysisService.updateFinalComment(userId, 1L, req);

        assertThat(response).isNotNull();
        assertThat(response.getFinalComment()).isEqualTo("User edited commentary");
        assertThat(response.getAiGeneratedComment()).isEqualTo("Original AI commentary");
        assertThat(response.getReviewStatus()).isEqualTo(AIReviewStatus.EDITED);
    }

    @Test
    @DisplayName("updateFinalComment throws InvalidAIAnalysisStateException if status is not COMPLETED")
    void testUpdateFinalComment_NotCompleted() {
        AIAnalysis analysis = AIAnalysis.builder()
                .id(1L)
                .company(company)
                .status(AIAnalysisStatus.PROCESSING)
                .build();

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(aiAnalysisRepository.findByIdAndCompanyId(1L, company.getId())).thenReturn(Optional.of(analysis));

        AIAnalysisFinalCommentRequest req = AIAnalysisFinalCommentRequest.builder().finalComment("New").build();

        assertThatThrownBy(() -> aiAnalysisService.updateFinalComment(userId, 1L, req))
                .isInstanceOf(InvalidAIAnalysisStateException.class);
    }

    @Test
    @DisplayName("approveAIAnalysis marks analysis as APPROVED")
    void testApproveAIAnalysis_Success() {
        AIAnalysis analysis = AIAnalysis.builder()
                .id(1L)
                .company(company)
                .assessment(assessment)
                .financialStatement(statement)
                .status(AIAnalysisStatus.COMPLETED)
                .reviewStatus(AIReviewStatus.PENDING)
                .build();

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(aiAnalysisRepository.findByIdAndCompanyId(1L, company.getId())).thenReturn(Optional.of(analysis));
        when(aiAnalysisRepository.save(any(AIAnalysis.class))).thenReturn(analysis);

        AIAnalysisResponse response = aiAnalysisService.approveAIAnalysis(userId, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getReviewStatus()).isEqualTo(AIReviewStatus.APPROVED);
    }

    @Test
    @DisplayName("rejectAIAnalysis marks analysis as REJECTED")
    void testRejectAIAnalysis_Success() {
        AIAnalysis analysis = AIAnalysis.builder()
                .id(1L)
                .company(company)
                .assessment(assessment)
                .financialStatement(statement)
                .status(AIAnalysisStatus.COMPLETED)
                .reviewStatus(AIReviewStatus.PENDING)
                .build();

        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(aiAnalysisRepository.findByIdAndCompanyId(1L, company.getId())).thenReturn(Optional.of(analysis));
        when(aiAnalysisRepository.save(any(AIAnalysis.class))).thenReturn(analysis);

        AIAnalysisResponse response = aiAnalysisService.rejectAIAnalysis(userId, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getReviewStatus()).isEqualTo(AIReviewStatus.REJECTED);
    }
}

