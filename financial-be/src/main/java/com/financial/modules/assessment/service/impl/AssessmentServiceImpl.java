package com.financial.modules.assessment.service.impl;

import com.financial.common.exception.*;
import com.financial.modules.assessment.dto.*;
import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.assessment.entity.AssessmentCategoryScore;
import com.financial.modules.assessment.entity.AssessmentDetail;
import com.financial.modules.assessment.enums.AssessmentStatus;
import com.financial.modules.assessment.enums.DataQualityStatus;
import com.financial.modules.assessment.enums.RatioEvaluationStatus;
import com.financial.modules.assessment.mapper.AssessmentMapper;
import com.financial.modules.assessment.repository.AssessmentCategoryScoreRepository;
import com.financial.modules.assessment.repository.AssessmentDetailRepository;
import com.financial.modules.assessment.repository.AssessmentRepository;
import com.financial.modules.assessment.service.AssessmentScoringService;
import com.financial.modules.assessment.service.AssessmentService;
import com.financial.modules.assessment.service.CategoryAggregationService;
import com.financial.modules.assessment.service.RatioEvaluationService;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.calculationengine.repository.FinancialCalculationRepository;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.repository.FinancialStatementRepository;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.enums.ValidationStatus;
import com.financial.modules.validation.repository.ValidationExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentDetailRepository detailRepository;
    private final AssessmentCategoryScoreRepository categoryScoreRepository;
    private final FinancialStatementRepository statementRepository;
    private final CompanyRepository companyRepository;
    private final FinancialCalculationRepository calculationRepository;
    private final ValidationExecutionRepository validationRepository;
    private final RatioEvaluationService ratioEvaluationService;
    private final CategoryAggregationService categoryAggregationService;
    private final AssessmentScoringService scoringService;
    private final AssessmentMapper mapper;

    @Override
    @Transactional
    public AssessmentResponse createAssessment(Long userId, Long statementId, AssessmentRequest request) {
        log.info("Creating financial health assessment for statement ID {} by user ID {}", statementId, userId);
        FinancialStatement statement = getOwnedStatement(userId, statementId);

        // Rule 1: Only assess confirmed financial statement data
        if (statement.getProcessingStatus() != FinancialStatementStatus.DATA_CONFIRMED) {
            log.warn("Assessment rejected: statement ID {} is in status {}", statementId, statement.getProcessingStatus());
            throw new UnconfirmedStatementException(statementId,
                    statement.getProcessingStatus() != null ? statement.getProcessingStatus().name() : "NULL");
        }

        // Rule 2: Retrieve compatible calculation execution
        FinancialCalculation calculation;
        if (request != null && request.getCalculationExecutionId() != null) {
            calculation = calculationRepository.findByIdAndFinancialStatementId(request.getCalculationExecutionId(), statementId)
                    .orElseThrow(() -> new CalculationNotFoundException(request.getCalculationExecutionId()));
        } else {
            calculation = calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId)
                    .orElseThrow(() -> new CalculationNotFoundException("No calculation execution found for confirmed statement ID: " + statementId));
        }

        // Calculation must contain valid calculated ratios
        if (calculation.getResults() == null || calculation.getResults().isEmpty()
                || calculation.getStatus() == CalculationStatus.FAILED) {
            throw new InvalidAssessmentStateException("Calculation execution ID " + calculation.getId() + " contains no valid ratio results.");
        }

        // Rule 3: Retrieve corresponding validation execution (if exists)
        ValidationExecution validationExecution;
        if (request != null && request.getValidationExecutionId() != null) {
            validationExecution = validationRepository.findByIdAndFinancialStatementId(request.getValidationExecutionId(), statementId)
                    .orElseThrow(() -> new ValidationNotFoundException(request.getValidationExecutionId()));
        } else {
            validationExecution = validationRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(statementId)
                    .orElse(null);
        }

        // Determine data quality status from validation execution
        DataQualityStatus dataQualityStatus = determineDataQualityStatus(calculation, validationExecution);

        // Rule 4: Evaluate individual financial ratios against active criteria
        List<AssessmentDetail> details = ratioEvaluationService.evaluateRatios(statement, calculation);

        // Rule 5: Aggregate results by the 4 financial categories
        List<AssessmentCategoryScore> categoryScores = categoryAggregationService.aggregateCategories(details);

        // Rule 6: Evaluate scoring & health classification (extensible, returns NOT_CONFIGURED when not configured)
        ScoringResult scoringResult = scoringService.evaluateScoring(details, categoryScores);

        // Tally ratio results
        int totalRatios = details.size();
        int passedRatios = 0;
        int failedRatios = 0;
        int insufficientDataRatios = 0;
        int notConfiguredRatios = 0;

        for (AssessmentDetail d : details) {
            if (d.getEvaluationStatus() == RatioEvaluationStatus.PASS) {
                passedRatios++;
            } else if (d.getEvaluationStatus() == RatioEvaluationStatus.FAIL) {
                failedRatios++;
            } else if (d.getEvaluationStatus() == RatioEvaluationStatus.INSUFFICIENT_DATA) {
                insufficientDataRatios++;
            } else if (d.getEvaluationStatus() == RatioEvaluationStatus.NOT_CONFIGURED) {
                notConfiguredRatios++;
            }
        }

        int nextVersion = assessmentRepository.countByFinancialStatementId(statementId) + 1;
        AssessmentStatus assessmentStatus = (insufficientDataRatios > 0)
                ? AssessmentStatus.INCOMPLETE
                : AssessmentStatus.COMPLETED;

        String notes = (request != null && request.getAssessmentNotes() != null)
                ? request.getAssessmentNotes()
                : "Financial health assessment executed successfully.";

        Assessment assessment = Assessment.builder()
                .financialStatement(statement)
                .company(statement.getCompany())
                .assessmentVersion(nextVersion)
                .confirmedDataVersion(calculation.getConfirmedDataVersion())
                .financialCalculation(calculation)
                .validationExecution(validationExecution)
                .assessmentStatus(assessmentStatus)
                .scoringStatus(scoringResult.getScoringStatus())
                .overallScore(scoringResult.getOverallScore())
                .healthClassification(scoringResult.getHealthClassification())
                .dataQualityStatus(dataQualityStatus)
                .totalRatios(totalRatios)
                .passedRatios(passedRatios)
                .failedRatios(failedRatios)
                .insufficientDataRatios(insufficientDataRatios)
                .notConfiguredRatios(notConfiguredRatios)
                .assessedByUserId(userId)
                .assessmentNotes(notes)
                .assessedAt(LocalDateTime.now())
                .build();

        for (AssessmentDetail d : details) {
            assessment.addDetail(d);
        }
        for (AssessmentCategoryScore cs : categoryScores) {
            assessment.addCategoryScore(cs);
        }

        Assessment saved = assessmentRepository.save(assessment);
        log.info("Persisted new assessment ID {} (version {}) for statement ID {}",
                saved.getId(), saved.getAssessmentVersion(), statementId);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssessmentHistoryResponse> getAssessmentHistory(Long userId, Long statementId, Pageable pageable) {
        getOwnedStatement(userId, statementId);
        Page<Assessment> page = assessmentRepository.findByFinancialStatementIdOrderByAssessmentVersionDesc(statementId, pageable);
        return page.map(mapper::toHistoryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentResponse getLatestAssessment(Long userId, Long statementId) {
        getOwnedStatement(userId, statementId);
        Assessment assessment = assessmentRepository.findTopByFinancialStatementIdOrderByAssessmentVersionDesc(statementId)
                .orElseThrow(() -> new AssessmentNotFoundException("No assessment found for financial statement ID: " + statementId));
        return mapper.toResponse(assessment);
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentResponse getAssessmentById(Long userId, Long statementId, Long assessmentId) {
        getOwnedStatement(userId, statementId);
        Assessment assessment = assessmentRepository.findByIdAndFinancialStatementId(assessmentId, statementId)
                .orElseThrow(() -> new AssessmentNotFoundException(assessmentId));
        return mapper.toResponse(assessment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentDetailResponse> getAssessmentDetails(Long userId, Long statementId, Long assessmentId) {
        getOwnedStatement(userId, statementId);
        if (!assessmentRepository.existsById(assessmentId)) {
            throw new AssessmentNotFoundException(assessmentId);
        }
        List<AssessmentDetail> details = detailRepository.findByAssessmentIdOrderByIdAsc(assessmentId);
        return details.stream().map(mapper::toDetailResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentCategoryResponse> getAssessmentCategories(Long userId, Long statementId, Long assessmentId) {
        getOwnedStatement(userId, statementId);
        if (!assessmentRepository.existsById(assessmentId)) {
            throw new AssessmentNotFoundException(assessmentId);
        }
        List<AssessmentCategoryScore> categoryScores = categoryScoreRepository.findByAssessmentIdOrderByIdAsc(assessmentId);
        return categoryScores.stream().map(mapper::toCategoryResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentSummaryResponse getAssessmentSummary(Long userId, Long statementId, Long assessmentId) {
        getOwnedStatement(userId, statementId);
        Assessment assessment = assessmentRepository.findByIdAndFinancialStatementId(assessmentId, statementId)
                .orElseThrow(() -> new AssessmentNotFoundException(assessmentId));
        return mapper.toSummaryResponse(assessment);
    }

    private DataQualityStatus determineDataQualityStatus(FinancialCalculation calc, ValidationExecution val) {
        if (val == null) {
            return DataQualityStatus.NOT_VALIDATED;
        }

        // Version mismatch check
        String calcVersion = calc.getConfirmedDataVersion();
        String valVersion = val.getConfirmedDataVersion();
        if (calcVersion != null && valVersion != null && !calcVersion.equalsIgnoreCase(valVersion)) {
            log.warn("Calculation version '{}' and validation version '{}' mismatch for statement ID {}",
                    calcVersion, valVersion, calc.getFinancialStatement().getId());
            return DataQualityStatus.REVIEW_REQUIRED;
        }

        if (val.getExecutionStatus() == ValidationStatus.FAIL || (val.getFailedRules() != null && val.getFailedRules() > 0)) {
            return DataQualityStatus.INVALID;
        }
        if (val.getExecutionStatus() == ValidationStatus.REVIEW_REQUIRED || (val.getReviewRequiredRules() != null && val.getReviewRequiredRules() > 0)) {
            return DataQualityStatus.REVIEW_REQUIRED;
        }
        if (val.getExecutionStatus() == ValidationStatus.WARNING || (val.getWarningRules() != null && val.getWarningRules() > 0)) {
            return DataQualityStatus.HAS_WARNINGS;
        }
        return DataQualityStatus.VALID;
    }

    private FinancialStatement getOwnedStatement(Long userId, Long statementId) {
        Company company = companyRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new StatementNotFoundException("User does not have an associated company"));

        return statementRepository.findByIdAndCompanyId(statementId, company.getId())
                .orElseThrow(() -> new StatementNotFoundException("Financial statement not found with ID: " + statementId));
    }
}

