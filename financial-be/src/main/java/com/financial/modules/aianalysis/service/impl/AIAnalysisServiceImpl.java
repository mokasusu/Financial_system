package com.financial.modules.aianalysis.service.impl;

import com.financial.common.exception.*;
import com.financial.modules.aianalysis.config.OpenRouterProperties;
import com.financial.modules.aianalysis.dto.*;
import com.financial.modules.aianalysis.dto.internal.AIAnalysisInputPayload;
import com.financial.modules.aianalysis.entity.AIAnalysis;
import com.financial.modules.aianalysis.enums.AIAnalysisStatus;
import com.financial.modules.aianalysis.enums.AIReviewStatus;
import com.financial.modules.aianalysis.mapper.AIAnalysisMapper;
import com.financial.modules.aianalysis.repository.AIAnalysisRepository;
import com.financial.modules.aianalysis.service.AIAnalysisAsyncProcessor;
import com.financial.modules.aianalysis.service.AIAnalysisPayloadBuilder;
import com.financial.modules.aianalysis.service.AIAnalysisService;
import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.assessment.repository.AssessmentRepository;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.repository.FinancialCalculationRepository;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.riskanalysis.entity.RiskAnalysis;
import com.financial.modules.riskanalysis.repository.RiskAnalysisRepository;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.repository.ValidationExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private final AIAnalysisRepository aiAnalysisRepository;
    private final AssessmentRepository assessmentRepository;
    private final CompanyRepository companyRepository;
    private final FinancialCalculationRepository calculationRepository;
    private final ValidationExecutionRepository validationRepository;
    private final RiskAnalysisRepository riskAnalysisRepository;
    private final AIAnalysisPayloadBuilder payloadBuilder;
    private final AIAnalysisAsyncProcessor asyncProcessor;
    private final AIAnalysisMapper mapper;
    private final OpenRouterProperties openRouterProperties;

    @Override
    @Transactional
    public AIAnalysisResponse generateAIAnalysis(Long userId, Long assessmentId, AIAnalysisGenerateRequest request) {
        log.info("Requesting AI financial analysis for assessment ID {} by user ID {}", assessmentId, userId);

        // 1. Verify user's company ownership
        Company company = getOwnedCompany(userId);

        // 2. Retrieve assessment and verify ownership
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new AssessmentNotFoundException("Assessment not found with ID: " + assessmentId));

        if (!assessment.getCompany().getId().equals(company.getId())) {
            log.warn("Security check failed: Assessment ID {} belongs to company ID {}, but user owns company ID {}",
                    assessmentId, assessment.getCompany().getId(), company.getId());
            throw new AssessmentNotFoundException("Assessment not found with ID: " + assessmentId);
        }

        // 3. Verify confirmed financial statement
        FinancialStatement statement = assessment.getFinancialStatement();
        if (statement == null) {
            throw new UpstreamDataUnavailableException("FINANCIAL_STATEMENT", "Financial statement associated with assessment is missing.");
        }
        if (statement.getProcessingStatus() != FinancialStatementStatus.DATA_CONFIRMED) {
            throw new UnconfirmedStatementException(statement.getId(),
                    statement.getProcessingStatus() != null ? statement.getProcessingStatus().name() : "NULL");
        }

        // 4. Retrieve Calculation Results
        FinancialCalculation calculation = assessment.getFinancialCalculation();
        if (calculation == null) {
            calculation = calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statement.getId())
                    .orElse(null);
        }
        if (calculation == null || calculation.getResults() == null || calculation.getResults().isEmpty()) {
            throw new UpstreamDataUnavailableException("CALCULATION", "Financial calculation results are missing or incomplete for assessment ID: " + assessmentId);
        }

        // 5. Retrieve Validation Execution
        ValidationExecution validation = assessment.getValidationExecution();
        if (validation == null) {
            validation = validationRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(statement.getId())
                    .orElse(null);
        }

        // 6. Retrieve Risk Analysis
        RiskAnalysis riskAnalysis = riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(statement.getId())
                .orElseThrow(() -> new UpstreamDataUnavailableException("RISK_ANALYSIS", "Risk analysis results are missing for financial statement ID: " + statement.getId() + ". Please execute risk analysis prior to AI analysis."));

        // 7. Prevent concurrent duplicate generation
        Optional<AIAnalysis> runningAnalysis = aiAnalysisRepository.findFirstByAssessmentIdAndStatusIn(
                assessmentId,
                List.of(AIAnalysisStatus.PENDING, AIAnalysisStatus.PROCESSING)
        );
        if (runningAnalysis.isPresent()) {
            throw new ConcurrentAIAnalysisException(assessmentId,
                    "An AI analysis is currently in progress for assessment ID: " + assessmentId + " (Status: " + runningAnalysis.get().getStatus() + ")");
        }

        // 8. Build structured input payload
        AIAnalysisInputPayload payload = payloadBuilder.buildPayload(
                assessment, statement, company, calculation, validation, riskAnalysis
        );

        // 9. Create AIAnalysis in PENDING status
        int nextVersion = aiAnalysisRepository.countByAssessmentId(assessmentId) + 1;
        String modelToUse = (request != null && request.getModelOverride() != null && !request.getModelOverride().isBlank())
                ? request.getModelOverride().trim()
                : openRouterProperties.getModel();

        AIAnalysis analysis = AIAnalysis.builder()
                .assessment(assessment)
                .financialStatement(statement)
                .company(company)
                .financialCalculation(calculation)
                .validationExecution(validation)
                .riskAnalysis(riskAnalysis)
                .analysisVersion(nextVersion)
                .confirmedDataVersion(assessment.getConfirmedDataVersion())
                .provider("OPENROUTER")
                .model(modelToUse)
                .status(AIAnalysisStatus.PENDING)
                .reviewStatus(AIReviewStatus.PENDING)
                .build();

        AIAnalysis saved = aiAnalysisRepository.save(analysis);
        log.info("Registered new AIAnalysis ID {} (version {}) in PENDING status. Triggering async task.", saved.getId(), saved.getAnalysisVersion());

        // 10. Trigger asynchronous processing
        asyncProcessor.processAIAnalysisAsync(saved.getId(), payload, modelToUse);

        return mapper.toAnalysisResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AIAnalysisResponse getLatestAIAnalysis(Long userId, Long assessmentId) {
        Company company = getOwnedCompany(userId);
        verifyAssessmentOwnership(assessmentId, company.getId());

        AIAnalysis analysis = aiAnalysisRepository.findTopByAssessmentIdOrderByAnalysisVersionDesc(assessmentId)
                .orElseThrow(() -> new AIAnalysisNotFoundException("No AI analysis found for assessment ID: " + assessmentId));

        return mapper.toAnalysisResponse(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public AIAnalysisResponse getAIAnalysisById(Long userId, Long analysisId) {
        Company company = getOwnedCompany(userId);
        AIAnalysis analysis = aiAnalysisRepository.findByIdAndCompanyId(analysisId, company.getId())
                .orElseThrow(() -> new AIAnalysisNotFoundException(analysisId));

        return mapper.toAnalysisResponse(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AIAnalysisHistoryResponse> getAIAnalysisHistory(Long userId, Long assessmentId, Pageable pageable) {
        Company company = getOwnedCompany(userId);
        verifyAssessmentOwnership(assessmentId, company.getId());

        Page<AIAnalysis> page = aiAnalysisRepository.findByAssessmentIdOrderByAnalysisVersionDesc(assessmentId, pageable);
        return page.map(mapper::toHistoryResponse);
    }

    @Override
    @Transactional
    public AIAnalysisResponse updateFinalComment(Long userId, Long analysisId, AIAnalysisFinalCommentRequest request) {
        Company company = getOwnedCompany(userId);
        AIAnalysis analysis = aiAnalysisRepository.findByIdAndCompanyId(analysisId, company.getId())
                .orElseThrow(() -> new AIAnalysisNotFoundException(analysisId));

        if (analysis.getStatus() != AIAnalysisStatus.COMPLETED) {
            throw new InvalidAIAnalysisStateException("Cannot update final comment for AI analysis in status: " + analysis.getStatus());
        }

        log.info("Updating final comment for AIAnalysis ID {}. Original AI comment preserved.", analysisId);
        analysis.setFinalComment(request.getFinalComment());
        analysis.setReviewStatus(AIReviewStatus.EDITED);

        AIAnalysis saved = aiAnalysisRepository.save(analysis);
        return mapper.toAnalysisResponse(saved);
    }

    @Override
    @Transactional
    public AIAnalysisResponse approveAIAnalysis(Long userId, Long analysisId) {
        Company company = getOwnedCompany(userId);
        AIAnalysis analysis = aiAnalysisRepository.findByIdAndCompanyId(analysisId, company.getId())
                .orElseThrow(() -> new AIAnalysisNotFoundException(analysisId));

        if (analysis.getStatus() != AIAnalysisStatus.COMPLETED) {
            throw new InvalidAIAnalysisStateException("Cannot approve AI analysis in status: " + analysis.getStatus());
        }

        log.info("Approving AIAnalysis ID {}", analysisId);
        analysis.setReviewStatus(AIReviewStatus.APPROVED);

        AIAnalysis saved = aiAnalysisRepository.save(analysis);
        return mapper.toAnalysisResponse(saved);
    }

    @Override
    @Transactional
    public AIAnalysisResponse rejectAIAnalysis(Long userId, Long analysisId) {
        Company company = getOwnedCompany(userId);
        AIAnalysis analysis = aiAnalysisRepository.findByIdAndCompanyId(analysisId, company.getId())
                .orElseThrow(() -> new AIAnalysisNotFoundException(analysisId));

        if (analysis.getStatus() != AIAnalysisStatus.COMPLETED) {
            throw new InvalidAIAnalysisStateException("Cannot reject AI analysis in status: " + analysis.getStatus());
        }

        log.info("Rejecting AIAnalysis ID {}", analysisId);
        analysis.setReviewStatus(AIReviewStatus.REJECTED);

        AIAnalysis saved = aiAnalysisRepository.save(analysis);
        return mapper.toAnalysisResponse(saved);
    }

    private Company getOwnedCompany(Long userId) {
        return companyRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new StatementNotFoundException("User does not have an associated active company"));
    }

    private void verifyAssessmentOwnership(Long assessmentId, Long companyId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new AssessmentNotFoundException("Assessment not found with ID: " + assessmentId));

        if (!assessment.getCompany().getId().equals(companyId)) {
            throw new AssessmentNotFoundException("Assessment not found with ID: " + assessmentId);
        }
    }
}

