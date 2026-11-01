package com.financial.modules.riskanalysis.service.impl;

import com.financial.common.exception.*;
import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.assessment.repository.AssessmentRepository;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.calculationengine.repository.FinancialCalculationRepository;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.repository.FinancialStatementRepository;
import com.financial.modules.riskanalysis.detector.RiskDetectionContext;
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
import com.financial.modules.riskanalysis.service.RiskAggregationService;
import com.financial.modules.riskanalysis.service.RiskAnalysisService;
import com.financial.modules.riskanalysis.service.RiskDetectionService;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.repository.ValidationExecutionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAnalysisServiceImpl implements RiskAnalysisService {

    private final RiskAnalysisRepository riskAnalysisRepository;
    private final IdentifiedRiskRepository identifiedRiskRepository;
    private final RiskRuleRepository riskRuleRepository;
    private final FinancialStatementRepository statementRepository;
    private final CompanyRepository companyRepository;
    private final AssessmentRepository assessmentRepository;
    private final FinancialCalculationRepository calculationRepository;
    private final ValidationExecutionRepository validationRepository;
    private final RiskDetectionService riskDetectionService;
    private final RiskAggregationService riskAggregationService;
    private final RiskAnalysisMapper mapper;

    @Override
    @Transactional
    public RiskAnalysisResponse analyzeRisks(Long userId, Long statementId, RiskAnalysisRequest request) {
        log.info("Executing financial risk analysis for statement ID {} by user ID {}", statementId, userId);
        FinancialStatement statement = getOwnedStatement(userId, statementId);

        // Rule 1: Draft data cannot be used
        if (statement.getProcessingStatus() != FinancialStatementStatus.DATA_CONFIRMED) {
            log.warn("Risk analysis rejected: statement ID {} is in status {}", statementId, statement.getProcessingStatus());
            throw new UnconfirmedStatementException(statementId,
                    statement.getProcessingStatus() != null ? statement.getProcessingStatus().name() : "NULL");
        }

        // Rule 2: Retrieve compatible assessment
        Assessment assessment;
        if (request != null && request.getAssessmentId() != null) {
            assessment = assessmentRepository.findByIdAndFinancialStatementId(request.getAssessmentId(), statementId)
                    .orElseThrow(() -> new AssessmentNotFoundException(request.getAssessmentId()));
        } else {
            assessment = assessmentRepository.findTopByFinancialStatementIdOrderByAssessmentVersionDesc(statementId)
                    .orElseThrow(() -> new AssessmentNotFoundException("No assessment found for statement ID: " + statementId));
        }

        // Rule 3: Retrieve compatible calculation
        FinancialCalculation calculation;
        if (request != null && request.getCalculationExecutionId() != null) {
            calculation = calculationRepository.findByIdAndFinancialStatementId(request.getCalculationExecutionId(), statementId)
                    .orElseThrow(() -> new CalculationNotFoundException(request.getCalculationExecutionId()));
        } else if (assessment.getFinancialCalculation() != null) {
            calculation = assessment.getFinancialCalculation();
        } else {
            calculation = calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId)
                    .orElseThrow(() -> new CalculationNotFoundException("No calculation found for statement ID: " + statementId));
        }

        if (calculation.getResults() == null || calculation.getResults().isEmpty()
                || calculation.getStatus() == CalculationStatus.FAILED) {
            throw new InvalidRiskAnalysisStateException("Calculation execution ID " + calculation.getId() + " contains no valid ratio results.");
        }

        // Rule 4: Retrieve corresponding validation execution (if exists)
        ValidationExecution validationExecution;
        if (request != null && request.getValidationExecutionId() != null) {
            validationExecution = validationRepository.findByIdAndFinancialStatementId(request.getValidationExecutionId(), statementId)
                    .orElseThrow(() -> new ValidationNotFoundException(request.getValidationExecutionId()));
        } else if (assessment.getValidationExecution() != null) {
            validationExecution = assessment.getValidationExecution();
        } else {
            validationExecution = validationRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(statementId).orElse(null);
        }

        // Rule 5: Mismatched confirmed data versions are rejected
        validateVersionCompatibility(calculation, assessment, validationExecution);

        // Rule 6: Retrieve active risk rules
        List<RiskRule> activeRules = riskRuleRepository.findByIsActiveTrue();

        // Retrieve previous latest analysis's risks for resolution detection
        Optional<RiskAnalysis> previousAnalysisOpt = riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(statementId);
        List<IdentifiedRisk> previousRisks = previousAnalysisOpt.map(RiskAnalysis::getIdentifiedRisks).orElse(Collections.emptyList());

        Map<String, FinancialCalculationResult> calcResultsByCode = calculation.getResults().stream()
                .filter(r -> r.getRatioCode() != null)
                .collect(Collectors.toMap(
                        FinancialCalculationResult::getRatioCode,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        RiskDetectionContext context = RiskDetectionContext.builder()
                .statement(statement)
                .assessment(assessment)
                .calculation(calculation)
                .validationExecution(validationExecution)
                .calculationResultsByCode(calcResultsByCode)
                .previousRisks(previousRisks)
                .build();

        // Rule 7: Detect risks across all categories
        List<IdentifiedRisk> identifiedRisks = riskDetectionService.detectAllRisks(context, activeRules);

        // Calculate risk counts
        int low = 0;
        int medium = 0;
        int high = 0;
        int critical = 0;
        int resolved = 0;
        int notConfigured = 0;
        int insufficient = 0;

        for (IdentifiedRisk r : identifiedRisks) {
            if (r.getRiskStatus() == RiskStatus.DETECTED || r.getRiskStatus() == RiskStatus.MONITORING) {
                if (r.getSeverity() != null) {
                    switch (r.getSeverity()) {
                        case LOW -> low++;
                        case MEDIUM -> medium++;
                        case HIGH -> high++;
                        case CRITICAL -> critical++;
                    }
                }
            } else if (r.getRiskStatus() == RiskStatus.RESOLVED) {
                resolved++;
            } else if (r.getRiskStatus() == RiskStatus.NOT_CONFIGURED) {
                notConfigured++;
            } else if (r.getRiskStatus() == RiskStatus.INSUFFICIENT_DATA) {
                insufficient++;
            }
        }

        int totalActive = low + medium + high + critical;
        int nextVersion = riskAnalysisRepository.countByFinancialStatementId(statementId) + 1;
        RiskAnalysisStatus status = (insufficient > 0) ? RiskAnalysisStatus.INCOMPLETE : RiskAnalysisStatus.COMPLETED;

        String notes = (request != null && request.getAnalysisNotes() != null)
                ? request.getAnalysisNotes()
                : "Financial risk analysis executed successfully.";

        RiskAnalysis analysis = RiskAnalysis.builder()
                .financialStatement(statement)
                .company(statement.getCompany())
                .assessment(assessment)
                .financialCalculation(calculation)
                .validationExecution(validationExecution)
                .analysisVersion(nextVersion)
                .confirmedDataVersion(calculation.getConfirmedDataVersion())
                .executionStatus(status)
                .totalRisks(totalActive)
                .lowRisks(low)
                .mediumRisks(medium)
                .highRisks(high)
                .criticalRisks(critical)
                .resolvedRisks(resolved)
                .notConfiguredCount(notConfigured)
                .insufficientDataCount(insufficient)
                .analysisNotes(notes)
                .analyzedAt(LocalDateTime.now())
                .build();

        for (IdentifiedRisk r : identifiedRisks) {
            analysis.addIdentifiedRisk(r);
        }

        RiskAnalysis saved = riskAnalysisRepository.save(analysis);
        log.info("Persisted new risk analysis ID {} (version {}) for statement ID {}. Active risks: {}",
                saved.getId(), saved.getAnalysisVersion(), statementId, totalActive);

        return mapper.toAnalysisResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RiskAnalysisHistoryResponse> getRiskAnalysisHistory(Long userId, Long statementId, Pageable pageable) {
        getOwnedStatement(userId, statementId);
        Page<RiskAnalysis> page = riskAnalysisRepository.findByFinancialStatementIdOrderByAnalysisVersionDesc(statementId, pageable);
        return page.map(mapper::toHistoryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RiskAnalysisResponse getLatestRiskAnalysis(Long userId, Long statementId) {
        getOwnedStatement(userId, statementId);
        RiskAnalysis analysis = riskAnalysisRepository.findTopByFinancialStatementIdOrderByAnalysisVersionDesc(statementId)
                .orElseThrow(() -> new RiskAnalysisNotFoundException("No risk analysis found for financial statement ID: " + statementId));
        return mapper.toAnalysisResponse(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public RiskAnalysisResponse getRiskAnalysisById(Long userId, Long statementId, Long analysisId) {
        getOwnedStatement(userId, statementId);
        RiskAnalysis analysis = riskAnalysisRepository.findByIdAndFinancialStatementId(analysisId, statementId)
                .orElseThrow(() -> new RiskAnalysisNotFoundException(analysisId));
        return mapper.toAnalysisResponse(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IdentifiedRiskResponse> getIdentifiedRisks(
            Long userId, Long statementId, Long analysisId,
            RiskCategory category, RiskSeverity severity, RiskStatus status
    ) {
        getOwnedStatement(userId, statementId);
        if (!riskAnalysisRepository.existsById(analysisId)) {
            throw new RiskAnalysisNotFoundException(analysisId);
        }

        Specification<IdentifiedRisk> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("riskAnalysis").get("id"), analysisId));

            if (category != null) {
                predicates.add(cb.equal(root.get("riskCategory"), category));
            }
            if (severity != null) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("riskStatus"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<IdentifiedRisk> risks = identifiedRiskRepository.findAll(spec);
        return risks.stream().map(mapper::toRiskResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RiskSummaryResponse getRiskSummary(Long userId, Long statementId, Long analysisId) {
        getOwnedStatement(userId, statementId);
        RiskAnalysis analysis = riskAnalysisRepository.findByIdAndFinancialStatementId(analysisId, statementId)
                .orElseThrow(() -> new RiskAnalysisNotFoundException(analysisId));
        return riskAggregationService.aggregateRiskSummary(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RiskRuleResponse> getRiskRules(
            RiskCategory category, String ratioCode, RiskSeverity severity, Boolean isActive, Pageable pageable
    ) {
        Specification<RiskRule> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null) {
                predicates.add(cb.equal(root.get("riskCategory"), category));
            }
            if (ratioCode != null && !ratioCode.isBlank()) {
                predicates.add(cb.equal(root.get("ratioCode"), ratioCode.trim()));
            }
            if (severity != null) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<RiskRule> page = riskRuleRepository.findAll(spec, pageable);
        return page.map(mapper::toRuleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RiskRuleResponse getRiskRuleById(Long id) {
        RiskRule rule = riskRuleRepository.findById(id)
                .orElseThrow(() -> new RiskRuleNotFoundException(id));
        return mapper.toRuleResponse(rule);
    }

    @Override
    @Transactional
    public RiskRuleResponse createRiskRule(RiskRuleRequest request) {
        if (riskRuleRepository.existsByRuleCode(request.getRuleCode())) {
            throw new DuplicateRiskRuleException("Risk rule with code '" + request.getRuleCode() + "' already exists");
        }
        RiskRule rule = mapper.toRuleEntity(request);
        RiskRule saved = riskRuleRepository.save(rule);
        log.info("Created new risk rule ID {} with code '{}'", saved.getId(), saved.getRuleCode());
        return mapper.toRuleResponse(saved);
    }

    @Override
    @Transactional
    public RiskRuleResponse updateRiskRule(Long id, RiskRuleRequest request) {
        RiskRule rule = riskRuleRepository.findById(id)
                .orElseThrow(() -> new RiskRuleNotFoundException(id));

        if (riskRuleRepository.existsByRuleCodeAndIdNot(request.getRuleCode(), id)) {
            throw new DuplicateRiskRuleException("Risk rule with code '" + request.getRuleCode() + "' already exists");
        }

        mapper.updateRuleEntity(rule, request);
        RiskRule saved = riskRuleRepository.save(rule);
        log.info("Updated risk rule ID {} ('{}')", saved.getId(), saved.getRuleCode());
        return mapper.toRuleResponse(saved);
    }

    @Override
    @Transactional
    public RiskRuleResponse updateRiskRuleStatus(Long id, Boolean isActive) {
        RiskRule rule = riskRuleRepository.findById(id)
                .orElseThrow(() -> new RiskRuleNotFoundException(id));
        rule.setIsActive(isActive);
        RiskRule saved = riskRuleRepository.save(rule);
        log.info("Updated risk rule ID {} active status to {}", saved.getId(), isActive);
        return mapper.toRuleResponse(saved);
    }

    private void validateVersionCompatibility(
            FinancialCalculation calculation,
            Assessment assessment,
            ValidationExecution validationExecution
    ) {
        String calcVersion = calculation.getConfirmedDataVersion();
        String assessmentVersion = assessment.getConfirmedDataVersion();

        if (calcVersion != null && assessmentVersion != null && !calcVersion.equalsIgnoreCase(assessmentVersion)) {
            throw new InvalidRiskAnalysisStateException(String.format(
                    "Confirmed data version mismatch between calculation ('%s') and assessment ('%s').",
                    calcVersion, assessmentVersion
            ));
        }

        if (validationExecution != null) {
            String valVersion = validationExecution.getConfirmedDataVersion();
            if (calcVersion != null && valVersion != null && !calcVersion.equalsIgnoreCase(valVersion)) {
                throw new InvalidRiskAnalysisStateException(String.format(
                        "Confirmed data version mismatch between calculation ('%s') and validation ('%s').",
                        calcVersion, valVersion
                ));
            }
        }
    }

    private FinancialStatement getOwnedStatement(Long userId, Long statementId) {
        Company company = companyRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new StatementNotFoundException("User does not have an associated company"));

        return statementRepository.findByIdAndCompanyId(statementId, company.getId())
                .orElseThrow(() -> new StatementNotFoundException("Financial statement not found with ID: " + statementId));
    }
}

