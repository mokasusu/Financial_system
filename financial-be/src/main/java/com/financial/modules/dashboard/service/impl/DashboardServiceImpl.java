package com.financial.modules.dashboard.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.exception.StatementNotFoundException;
import com.financial.modules.aianalysis.entity.*;
import com.financial.modules.aianalysis.repository.AIAnalysisRepository;
import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.assessment.entity.AssessmentDetail;
import com.financial.modules.assessment.repository.AssessmentRepository;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.calculationengine.repository.FinancialCalculationRepository;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.dashboard.dto.*;
import com.financial.modules.dashboard.service.DashboardService;
import com.financial.modules.evaluationcriteria.entity.EvaluationCriteria;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.evaluationcriteria.repository.EvaluationCriteriaRepository;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.repository.FinancialStatementRepository;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskAnalysis;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import com.financial.modules.riskanalysis.repository.RiskAnalysisRepository;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.validation.repository.ValidationExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CompanyRepository companyRepository;
    private final FinancialStatementRepository statementRepository;
    private final FinancialCalculationRepository calculationRepository;
    private final AssessmentRepository assessmentRepository;
    private final ValidationExecutionRepository validationExecutionRepository;
    private final RiskAnalysisRepository riskAnalysisRepository;
    private final AIAnalysisRepository aiAnalysisRepository;
    private final EvaluationCriteriaRepository evaluationCriteriaRepository;
    private final ObjectMapper objectMapper;

    private static final List<String> CORE_KPIS = List.of(
            "CURRENT_RATIO",
            "DEBT_TO_EQUITY",
            "ROA",
            "ROE"
    );

    private static final Map<String, String> KPI_NAMES = Map.of(
            "CURRENT_RATIO", "Khả năng thanh toán hiện hành",
            "DEBT_TO_EQUITY", "Hệ số nợ trên vốn chủ sở hữu",
            "ROA", "Tỷ suất sinh lời trên tổng tài sản",
            "ROE", "Tỷ suất sinh lời trên vốn chủ sở hữu"
    );

    private static final Map<String, List<String>> CATEGORY_RATIOS = new LinkedHashMap<>();

    static {
        CATEGORY_RATIOS.put("LIQUIDITY", List.of("CURRENT_RATIO", "QUICK_RATIO", "CASH_RATIO"));
        CATEGORY_RATIOS.put("SOLVENCY", List.of("DEBT_TO_EQUITY", "DEBT_RATIO", "EQUITY_RATIO"));
        CATEGORY_RATIOS.put("PROFITABILITY", List.of("ROA", "ROE", "NET_PROFIT_MARGIN", "GROSS_PROFIT_MARGIN"));
        CATEGORY_RATIOS.put("EFFICIENCY", List.of("ASSET_TURNOVER", "INVENTORY_TURNOVER", "RECEIVABLES_TURNOVER", "PAYABLES_TURNOVER"));
    }

    private static final Map<String, String> CATEGORY_NAMES = Map.of(
            "LIQUIDITY", "Thanh khoản",
            "SOLVENCY", "Đòn bẩy & Khả năng trả nợ",
            "PROFITABILITY", "Hiệu suất sinh lời",
            "EFFICIENCY", "Hiệu quả hoạt động"
    );

    private static final Map<String, String> ALL_RATIO_NAMES = new HashMap<>();

    static {
        ALL_RATIO_NAMES.put("CURRENT_RATIO", "Khả năng thanh toán hiện hành");
        ALL_RATIO_NAMES.put("QUICK_RATIO", "Khả năng thanh toán nhanh");
        ALL_RATIO_NAMES.put("CASH_RATIO", "Khả năng thanh toán tức thời");
        ALL_RATIO_NAMES.put("DEBT_TO_EQUITY", "Hệ số nợ trên vốn chủ sở hữu");
        ALL_RATIO_NAMES.put("DEBT_RATIO", "Hệ số nợ");
        ALL_RATIO_NAMES.put("EQUITY_RATIO", "Hệ số vốn chủ sở hữu");
        ALL_RATIO_NAMES.put("ROA", "Tỷ suất sinh lời trên tổng tài sản");
        ALL_RATIO_NAMES.put("ROE", "Tỷ suất sinh lời trên vốn chủ sở hữu");
        ALL_RATIO_NAMES.put("NET_PROFIT_MARGIN", "Biên lợi nhuận ròng");
        ALL_RATIO_NAMES.put("GROSS_PROFIT_MARGIN", "Biên lợi nhuận gộp");
        ALL_RATIO_NAMES.put("ASSET_TURNOVER", "Vòng quay tổng tài sản");
        ALL_RATIO_NAMES.put("INVENTORY_TURNOVER", "Vòng quay hàng tồn kho");
        ALL_RATIO_NAMES.put("RECEIVABLES_TURNOVER", "Vòng quay khoản phải thu");
        ALL_RATIO_NAMES.put("PAYABLES_TURNOVER", "Vòng quay khoản phải trả");
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewResponse getDashboardOverview(Long userId, String period, Long financialStatementId) {
        log.info("Aggregating complete dashboard overview for user ID {}, period: {}, statementId: {}", userId, period, financialStatementId);

        Optional<Company> companyOpt = companyRepository.findByUserIdAndDeletedFalse(userId);
        if (companyOpt.isEmpty()) {
            return buildEmptyCompanyDashboard();
        }

        Company company = companyOpt.get();
        FinancialStatement statement = resolveStatement(company, period, financialStatementId);
        if (statement == null) {
            return buildEmptyStatementDashboard(company);
        }

        DashboardContextResponse context = buildContext(company, statement);
        boolean isConfirmed = statement.getProcessingStatus() == FinancialStatementStatus.DATA_CONFIRMED;

        // Validation execution can be present even for statements in review
        ValidationExecution validationExecution = validationExecutionRepository
                .findTopByFinancialStatementIdOrderByExecutedAtDesc(statement.getId()).orElse(null);
        DashboardValidationResponse validationResponse = buildValidationResponse(validationExecution, statement);

        if (!isConfirmed) {
            DashboardDataStatusDTO dataStatuses = DashboardDataStatusDTO.builder()
                    .companyStatus("READY")
                    .statementStatus(statement.getProcessingStatus().name())
                    .calculationStatus("STATEMENT_NOT_CONFIRMED")
                    .validationStatus(validationExecution != null ? "READY" : "VALIDATION_NOT_AVAILABLE")
                    .assessmentStatus("STATEMENT_NOT_CONFIRMED")
                    .riskStatus("STATEMENT_NOT_CONFIRMED")
                    .aiStatus("STATEMENT_NOT_CONFIRMED")
                    .build();

            return DashboardOverviewResponse.builder()
                    .context(context)
                    .dataStatuses(dataStatuses)
                    .coreKpis(Collections.emptyList())
                    .financialRatios(Collections.emptyList())
                    .visualAnalytics(DashboardVisualAnalyticsResponse.builder()
                            .ratioComparisons(Collections.emptyList())
                            .capitalStructure(extractCapitalStructure(statement, Collections.emptyMap()))
                            .categoryOverview(Collections.emptyList())
                            .build())
                    .validation(validationResponse)
                    .riskOverview(buildEmptyRiskResponse("STATEMENT_NOT_CONFIRMED"))
                    .aiCommentary(buildEmptyAICommentaryResponse("STATEMENT_NOT_CONFIRMED"))
                    .build();
        }

        // Statement is confirmed: load upstream records
        FinancialCalculation calculation = calculationRepository
                .findTopByFinancialStatementIdOrderByCalculationVersionDesc(statement.getId()).orElse(null);
        Assessment assessment = assessmentRepository
                .findTopByFinancialStatementIdOrderByAssessmentVersionDesc(statement.getId()).orElse(null);
        RiskAnalysis riskAnalysis = riskAnalysisRepository
                .findTopByFinancialStatementIdOrderByAnalysisVersionDesc(statement.getId()).orElse(null);
        AIAnalysis aiAnalysis = null;
        if (assessment != null) {
            aiAnalysis = aiAnalysisRepository
                    .findTopByAssessmentIdOrderByAnalysisVersionDesc(assessment.getId()).orElse(null);
        }

        Map<String, FinancialCalculationResult> calcMap = extractCalcMap(calculation);
        Map<String, AssessmentDetail> detailMap = extractDetailMap(assessment);

        List<DashboardKPIResponse> coreKpis = buildCoreKpis(calcMap, detailMap);
        List<DashboardCategoryResponse> categories = buildCategoryResponses(calcMap, detailMap);
        DashboardVisualAnalyticsResponse visualAnalytics = buildVisualAnalytics(calcMap, detailMap, statement, categories);
        DashboardRiskResponse riskResponse = buildRiskResponse(riskAnalysis, null, null, null);
        DashboardAICommentaryResponse aiCommentary = buildAICommentaryResponse(aiAnalysis);

        DashboardDataStatusDTO dataStatuses = DashboardDataStatusDTO.builder()
                .companyStatus("READY")
                .statementStatus("READY")
                .calculationStatus(calculation != null ? "READY" : "CALCULATION_NOT_AVAILABLE")
                .validationStatus(validationExecution != null ? "READY" : "VALIDATION_NOT_AVAILABLE")
                .assessmentStatus(assessment != null ? "READY" : "ASSESSMENT_NOT_AVAILABLE")
                .riskStatus(riskAnalysis != null ? "READY" : "RISK_ANALYSIS_NOT_AVAILABLE")
                .aiStatus(aiAnalysis != null ? "READY" : "AI_ANALYSIS_NOT_AVAILABLE")
                .build();

        return DashboardOverviewResponse.builder()
                .context(context)
                .dataStatuses(dataStatuses)
                .coreKpis(coreKpis)
                .financialRatios(categories)
                .visualAnalytics(visualAnalytics)
                .validation(validationResponse)
                .riskOverview(riskResponse)
                .aiCommentary(aiCommentary)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardKPIResponse> getCoreKpis(Long userId, String period, Long financialStatementId) {
        log.info("Retrieving core KPIs for user ID {}", userId);
        Optional<Company> companyOpt = companyRepository.findByUserIdAndDeletedFalse(userId);
        if (companyOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Company company = companyOpt.get();
        FinancialStatement statement = resolveStatement(company, period, financialStatementId);
        if (statement == null || statement.getProcessingStatus() != FinancialStatementStatus.DATA_CONFIRMED) {
            return Collections.emptyList();
        }

        FinancialCalculation calculation = calculationRepository
                .findTopByFinancialStatementIdOrderByCalculationVersionDesc(statement.getId()).orElse(null);
        Assessment assessment = assessmentRepository
                .findTopByFinancialStatementIdOrderByAssessmentVersionDesc(statement.getId()).orElse(null);

        Map<String, FinancialCalculationResult> calcMap = extractCalcMap(calculation);
        Map<String, AssessmentDetail> detailMap = extractDetailMap(assessment);

        return buildCoreKpis(calcMap, detailMap);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardCategoryResponse> getFinancialRatios(Long userId, String period, Long financialStatementId) {
        log.info("Retrieving all 14 financial ratios for user ID {}", userId);
        Optional<Company> companyOpt = companyRepository.findByUserIdAndDeletedFalse(userId);
        if (companyOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Company company = companyOpt.get();
        FinancialStatement statement = resolveStatement(company, period, financialStatementId);
        if (statement == null || statement.getProcessingStatus() != FinancialStatementStatus.DATA_CONFIRMED) {
            return Collections.emptyList();
        }

        FinancialCalculation calculation = calculationRepository
                .findTopByFinancialStatementIdOrderByCalculationVersionDesc(statement.getId()).orElse(null);
        Assessment assessment = assessmentRepository
                .findTopByFinancialStatementIdOrderByAssessmentVersionDesc(statement.getId()).orElse(null);

        Map<String, FinancialCalculationResult> calcMap = extractCalcMap(calculation);
        Map<String, AssessmentDetail> detailMap = extractDetailMap(assessment);

        return buildCategoryResponses(calcMap, detailMap);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardVisualAnalyticsResponse getVisualAnalytics(Long userId, String period, Long financialStatementId) {
        log.info("Retrieving visual analytics data for user ID {}", userId);
        Optional<Company> companyOpt = companyRepository.findByUserIdAndDeletedFalse(userId);
        if (companyOpt.isEmpty()) {
            return DashboardVisualAnalyticsResponse.builder()
                    .ratioComparisons(Collections.emptyList())
                    .capitalStructure(null)
                    .categoryOverview(Collections.emptyList())
                    .build();
        }

        Company company = companyOpt.get();
        FinancialStatement statement = resolveStatement(company, period, financialStatementId);
        if (statement == null) {
            return DashboardVisualAnalyticsResponse.builder()
                    .ratioComparisons(Collections.emptyList())
                    .capitalStructure(null)
                    .categoryOverview(Collections.emptyList())
                    .build();
        }

        if (statement.getProcessingStatus() != FinancialStatementStatus.DATA_CONFIRMED) {
            return DashboardVisualAnalyticsResponse.builder()
                    .ratioComparisons(Collections.emptyList())
                    .capitalStructure(extractCapitalStructure(statement, Collections.emptyMap()))
                    .categoryOverview(Collections.emptyList())
                    .build();
        }

        FinancialCalculation calculation = calculationRepository
                .findTopByFinancialStatementIdOrderByCalculationVersionDesc(statement.getId()).orElse(null);
        Assessment assessment = assessmentRepository
                .findTopByFinancialStatementIdOrderByAssessmentVersionDesc(statement.getId()).orElse(null);

        Map<String, FinancialCalculationResult> calcMap = extractCalcMap(calculation);
        Map<String, AssessmentDetail> detailMap = extractDetailMap(assessment);
        List<DashboardCategoryResponse> categories = buildCategoryResponses(calcMap, detailMap);

        return buildVisualAnalytics(calcMap, detailMap, statement, categories);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardValidationResponse getValidationOverview(Long userId, String period, Long financialStatementId) {
        log.info("Retrieving validation overview for user ID {}", userId);
        Optional<Company> companyOpt = companyRepository.findByUserIdAndDeletedFalse(userId);
        if (companyOpt.isEmpty()) {
            return buildEmptyValidationResponse("NO_COMPANY");
        }

        Company company = companyOpt.get();
        FinancialStatement statement = resolveStatement(company, period, financialStatementId);
        if (statement == null) {
            return buildEmptyValidationResponse("NO_FINANCIAL_STATEMENT");
        }

        ValidationExecution validationExecution = validationExecutionRepository
                .findTopByFinancialStatementIdOrderByExecutedAtDesc(statement.getId()).orElse(null);
        return buildValidationResponse(validationExecution, statement);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardRiskResponse getRiskOverview(Long userId, String period, Long financialStatementId,
                                                 RiskSeverity severity, RiskCategory category, RiskStatus status) {
        log.info("Retrieving risk overview for user ID {}", userId);
        Optional<Company> companyOpt = companyRepository.findByUserIdAndDeletedFalse(userId);
        if (companyOpt.isEmpty()) {
            return buildEmptyRiskResponse("NO_COMPANY");
        }

        Company company = companyOpt.get();
        FinancialStatement statement = resolveStatement(company, period, financialStatementId);
        if (statement == null) {
            return buildEmptyRiskResponse("NO_FINANCIAL_STATEMENT");
        }

        if (statement.getProcessingStatus() != FinancialStatementStatus.DATA_CONFIRMED) {
            return buildEmptyRiskResponse("STATEMENT_NOT_CONFIRMED");
        }

        RiskAnalysis riskAnalysis = riskAnalysisRepository
                .findTopByFinancialStatementIdOrderByAnalysisVersionDesc(statement.getId()).orElse(null);
        return buildRiskResponse(riskAnalysis, severity, category, status);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardAICommentaryResponse getAICommentary(Long userId, String period, Long financialStatementId) {
        log.info("Retrieving AI commentary for user ID {}", userId);
        Optional<Company> companyOpt = companyRepository.findByUserIdAndDeletedFalse(userId);
        if (companyOpt.isEmpty()) {
            return buildEmptyAICommentaryResponse("NO_COMPANY");
        }

        Company company = companyOpt.get();
        FinancialStatement statement = resolveStatement(company, period, financialStatementId);
        if (statement == null) {
            return buildEmptyAICommentaryResponse("NO_FINANCIAL_STATEMENT");
        }

        if (statement.getProcessingStatus() != FinancialStatementStatus.DATA_CONFIRMED) {
            return buildEmptyAICommentaryResponse("STATEMENT_NOT_CONFIRMED");
        }

        Assessment assessment = assessmentRepository
                .findTopByFinancialStatementIdOrderByAssessmentVersionDesc(statement.getId()).orElse(null);
        if (assessment == null) {
            return buildEmptyAICommentaryResponse("ASSESSMENT_NOT_AVAILABLE");
        }

        AIAnalysis aiAnalysis = aiAnalysisRepository
                .findTopByAssessmentIdOrderByAnalysisVersionDesc(assessment.getId()).orElse(null);
        return buildAICommentaryResponse(aiAnalysis);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardTrendResponse getFinancialTrends(Long userId, String fromPeriod, String toPeriod, List<String> ratioCodes) {
        log.info("Retrieving financial trends for user ID {}, fromPeriod: {}, toPeriod: {}", userId, fromPeriod, toPeriod);
        Optional<Company> companyOpt = companyRepository.findByUserIdAndDeletedFalse(userId);
        if (companyOpt.isEmpty()) {
            return DashboardTrendResponse.builder()
                    .companyId(null)
                    .companyName(null)
                    .availablePeriods(Collections.emptyList())
                    .trendData(Collections.emptyList())
                    .build();
        }

        Company company = companyOpt.get();
        List<String> targetCodes = (ratioCodes != null && !ratioCodes.isEmpty())
                ? ratioCodes.stream().map(String::toUpperCase).toList()
                : CORE_KPIS;

        // Fetch all confirmed financial statements of the company ordered by period / id ascending
        Page<FinancialStatement> statementPage = statementRepository.findByCompanyIdWithFilters(
                company.getId(),
                null,
                null,
                FinancialStatementStatus.DATA_CONFIRMED,
                PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "fiscalYear", "period", "id"))
        );

        List<FinancialStatement> statements = statementPage.getContent();
        if (statements.isEmpty()) {
            return DashboardTrendResponse.builder()
                    .companyId(company.getId())
                    .companyName(company.getCompanyName())
                    .availablePeriods(Collections.emptyList())
                    .trendData(Collections.emptyList())
                    .build();
        }

        List<String> availablePeriods = new ArrayList<>();
        List<TrendDataPointDTO> trendData = new ArrayList<>();

        for (FinancialStatement stmt : statements) {
            String p = stmt.getPeriod();
            if (p == null || p.isBlank()) p = String.valueOf(stmt.getFiscalYear());

            // Optional range filtering
            if (fromPeriod != null && !fromPeriod.isBlank() && p.compareToIgnoreCase(fromPeriod) < 0) {
                continue;
            }
            if (toPeriod != null && !toPeriod.isBlank() && p.compareToIgnoreCase(toPeriod) > 0) {
                continue;
            }

            if (!availablePeriods.contains(p)) {
                availablePeriods.add(p);
            }

            Optional<FinancialCalculation> calcOpt = calculationRepository
                    .findTopByFinancialStatementIdOrderByCalculationVersionDesc(stmt.getId());

            if (calcOpt.isPresent()) {
                FinancialCalculation calc = calcOpt.get();
                if (calc.getResults() != null) {
                    for (FinancialCalculationResult res : calc.getResults()) {
                        if (res.getRatioCode() != null && targetCodes.contains(res.getRatioCode().toUpperCase())) {
                            trendData.add(TrendDataPointDTO.builder()
                                    .reportingPeriod(p)
                                    .financialStatementId(stmt.getId())
                                    .calculationId(calc.getId())
                                    .ratioCode(res.getRatioCode())
                                    .ratioName(res.getRatioName() != null ? res.getRatioName() : ALL_RATIO_NAMES.get(res.getRatioCode()))
                                    .category(res.getCategory() != null ? res.getCategory().name() : null)
                                    .value(res.getResultValue())
                                    .unit(res.getUnit() != null ? res.getUnit().name() : null)
                                    .calculationStatus(res.getStatus() != null ? res.getStatus().name() : null)
                                    .build());
                        }
                    }
                }
            }
        }

        return DashboardTrendResponse.builder()
                .companyId(company.getId())
                .companyName(company.getCompanyName())
                .availablePeriods(availablePeriods)
                .trendData(trendData)
                .build();
    }

    // ---------------------------------------------------------------------------------------------
    // Helper Methods
    // ---------------------------------------------------------------------------------------------

    private FinancialStatement resolveStatement(Company company, String period, Long statementId) {
        if (statementId != null) {
            return statementRepository.findByIdAndCompanyId(statementId, company.getId())
                    .orElseThrow(() -> new StatementNotFoundException("Financial statement not found with ID: " + statementId + " for your company."));
        }

        if (period != null && !period.isBlank()) {
            Page<FinancialStatement> page = statementRepository.findByCompanyIdWithFilters(
                    company.getId(), null, period.trim(), null,
                    PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id"))
            );
            if (page.hasContent()) {
                return page.getContent().get(0);
            }
        }

        // Try latest confirmed statement first
        Page<FinancialStatement> confirmedPage = statementRepository.findByCompanyIdWithFilters(
                company.getId(), null, null, FinancialStatementStatus.DATA_CONFIRMED,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id"))
        );
        if (confirmedPage.hasContent()) {
            return confirmedPage.getContent().get(0);
        }

        // Fallback to latest statement of any status
        Page<FinancialStatement> anyPage = statementRepository.findByCompanyIdWithFilters(
                company.getId(), null, null, null,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id"))
        );
        return anyPage.hasContent() ? anyPage.getContent().get(0) : null;
    }

    private DashboardContextResponse buildContext(Company company, FinancialStatement statement) {
        return DashboardContextResponse.builder()
                .companyId(company.getId())
                .companyName(company.getCompanyName())
                .taxCode(company.getTaxCode())
                .businessSector(company.getMainBusinessLine())
                .accountingStandard(statement.getAccountingStandard())
                .selectedPeriod(statement.getPeriod())
                .currencyUnit(statement.getUnit() != null ? statement.getUnit() : "VND")
                .financialStatementId(statement.getId())
                .financialStatementProcessingStatus(statement.getProcessingStatus().name())
                .hasCompany(true)
                .hasFinancialStatement(true)
                .dataStatus(statement.getProcessingStatus() == FinancialStatementStatus.DATA_CONFIRMED ? "READY" : statement.getProcessingStatus().name())
                .build();
    }

    private DashboardOverviewResponse buildEmptyCompanyDashboard() {
        DashboardContextResponse context = DashboardContextResponse.builder()
                .hasCompany(false)
                .hasFinancialStatement(false)
                .dataStatus("NO_COMPANY")
                .build();

        DashboardDataStatusDTO statuses = DashboardDataStatusDTO.builder()
                .companyStatus("NO_COMPANY")
                .statementStatus("NO_COMPANY")
                .calculationStatus("NO_COMPANY")
                .validationStatus("NO_COMPANY")
                .assessmentStatus("NO_COMPANY")
                .riskStatus("NO_COMPANY")
                .aiStatus("NO_COMPANY")
                .build();

        return DashboardOverviewResponse.builder()
                .context(context)
                .dataStatuses(statuses)
                .coreKpis(Collections.emptyList())
                .financialRatios(Collections.emptyList())
                .visualAnalytics(DashboardVisualAnalyticsResponse.builder()
                        .ratioComparisons(Collections.emptyList())
                        .capitalStructure(null)
                        .categoryOverview(Collections.emptyList())
                        .build())
                .validation(buildEmptyValidationResponse("NO_COMPANY"))
                .riskOverview(buildEmptyRiskResponse("NO_COMPANY"))
                .aiCommentary(buildEmptyAICommentaryResponse("NO_COMPANY"))
                .build();
    }

    private DashboardOverviewResponse buildEmptyStatementDashboard(Company company) {
        DashboardContextResponse context = DashboardContextResponse.builder()
                .companyId(company.getId())
                .companyName(company.getCompanyName())
                .taxCode(company.getTaxCode())
                .businessSector(company.getMainBusinessLine())
                .hasCompany(true)
                .hasFinancialStatement(false)
                .dataStatus("NO_FINANCIAL_STATEMENT")
                .build();

        DashboardDataStatusDTO statuses = DashboardDataStatusDTO.builder()
                .companyStatus("READY")
                .statementStatus("NO_FINANCIAL_STATEMENT")
                .calculationStatus("NO_FINANCIAL_STATEMENT")
                .validationStatus("NO_FINANCIAL_STATEMENT")
                .assessmentStatus("NO_FINANCIAL_STATEMENT")
                .riskStatus("NO_FINANCIAL_STATEMENT")
                .aiStatus("NO_FINANCIAL_STATEMENT")
                .build();

        return DashboardOverviewResponse.builder()
                .context(context)
                .dataStatuses(statuses)
                .coreKpis(Collections.emptyList())
                .financialRatios(Collections.emptyList())
                .visualAnalytics(DashboardVisualAnalyticsResponse.builder()
                        .ratioComparisons(Collections.emptyList())
                        .capitalStructure(null)
                        .categoryOverview(Collections.emptyList())
                        .build())
                .validation(buildEmptyValidationResponse("NO_FINANCIAL_STATEMENT"))
                .riskOverview(buildEmptyRiskResponse("NO_FINANCIAL_STATEMENT"))
                .aiCommentary(buildEmptyAICommentaryResponse("NO_FINANCIAL_STATEMENT"))
                .build();
    }

    private Map<String, FinancialCalculationResult> extractCalcMap(FinancialCalculation calculation) {
        if (calculation == null || calculation.getResults() == null) {
            return Collections.emptyMap();
        }
        return calculation.getResults().stream()
                .filter(r -> r.getRatioCode() != null)
                .collect(Collectors.toMap(
                        r -> r.getRatioCode().toUpperCase(),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    private Map<String, AssessmentDetail> extractDetailMap(Assessment assessment) {
        if (assessment == null || assessment.getDetails() == null) {
            return Collections.emptyMap();
        }
        return assessment.getDetails().stream()
                .filter(d -> d.getRatioCodeSnapshot() != null)
                .collect(Collectors.toMap(
                        d -> d.getRatioCodeSnapshot().toUpperCase(),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    private List<DashboardKPIResponse> buildCoreKpis(
            Map<String, FinancialCalculationResult> calcMap,
            Map<String, AssessmentDetail> detailMap
    ) {
        List<DashboardKPIResponse> kpis = new ArrayList<>();

        for (String code : CORE_KPIS) {
            FinancialCalculationResult calc = calcMap.get(code);
            AssessmentDetail detail = detailMap.get(code);

            String name = (calc != null && calc.getRatioName() != null)
                    ? calc.getRatioName()
                    : KPI_NAMES.getOrDefault(code, code);

            BigDecimal value = null;
            String unit = null;
            String calcStatus = "CALCULATION_NOT_AVAILABLE";
            String explanation = null;

            if (calc != null) {
                value = calc.getResultValue();
                unit = calc.getUnit() != null ? calc.getUnit().name() : null;
                calcStatus = calc.getStatus() != null ? calc.getStatus().name() : "SUCCESS";
                explanation = calc.getCalculationNote();
            }

            BigDecimal threshold = null;
            String operator = null;
            String evalStatus = "NOT_CONFIGURED";

            if (detail != null) {
                threshold = detail.getThresholdValueSnapshot();
                operator = toOperatorSymbol(detail.getComparisonOperatorSnapshot());
                evalStatus = detail.getEvaluationStatus() != null ? detail.getEvaluationStatus().name() : "NOT_CONFIGURED";
                if (detail.getEvaluationExplanation() != null && !detail.getEvaluationExplanation().isBlank()) {
                    explanation = detail.getEvaluationExplanation();
                }
            } else {
                // Lookup criteria if assessment not run yet
                List<EvaluationCriteria> criteriaList = evaluationCriteriaRepository
                        .findByRatioDefinitionRatioCodeAndIsActiveTrue(code);
                if (!criteriaList.isEmpty()) {
                    EvaluationCriteria ec = criteriaList.get(0);
                    threshold = ec.getThresholdValue();
                    operator = toOperatorSymbol(ec.getComparisonOperator());
                    evalStatus = "NOT_EVALUATED";
                }
            }

            if (value == null && explanation == null) {
                explanation = "Chưa có đủ số liệu BCTC đã xác nhận để tính toán chỉ số này.";
            }

            kpis.add(DashboardKPIResponse.builder()
                    .code(code)
                    .name(name)
                    .value(value)
                    .unit(unit)
                    .calculationStatus(calcStatus)
                    .evaluationStatus(evalStatus)
                    .configuredThreshold(threshold)
                    .comparisonOperator(operator)
                    .explanation(explanation)
                    .build());
        }

        return kpis;
    }

    private List<DashboardCategoryResponse> buildCategoryResponses(
            Map<String, FinancialCalculationResult> calcMap,
            Map<String, AssessmentDetail> detailMap
    ) {
        List<DashboardCategoryResponse> categories = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : CATEGORY_RATIOS.entrySet()) {
            String catCode = entry.getKey();
            String catName = CATEGORY_NAMES.getOrDefault(catCode, catCode);
            List<DashboardRatioResponse> ratioResponses = new ArrayList<>();

            for (String code : entry.getValue()) {
                FinancialCalculationResult calc = calcMap.get(code);
                AssessmentDetail detail = detailMap.get(code);

                String name = (calc != null && calc.getRatioName() != null)
                        ? calc.getRatioName()
                        : ALL_RATIO_NAMES.getOrDefault(code, code);

                BigDecimal value = null;
                String unit = null;
                String calcStatus = "CALCULATION_NOT_AVAILABLE";
                String missingExp = null;

                if (calc != null) {
                    value = calc.getResultValue();
                    unit = calc.getUnit() != null ? calc.getUnit().name() : null;
                    calcStatus = calc.getStatus() != null ? calc.getStatus().name() : "SUCCESS";
                    if (value == null) {
                        missingExp = calc.getCalculationNote() != null
                                ? calc.getCalculationNote()
                                : "Chưa có đủ số liệu BCTC để tính toán.";
                    }
                } else {
                    missingExp = "Chưa có số liệu tính toán từ Calculation Engine.";
                }

                BigDecimal threshold = null;
                String operator = null;
                String evalStatus = "NOT_CONFIGURED";

                if (detail != null) {
                    threshold = detail.getThresholdValueSnapshot();
                    operator = toOperatorSymbol(detail.getComparisonOperatorSnapshot());
                    evalStatus = detail.getEvaluationStatus() != null ? detail.getEvaluationStatus().name() : "NOT_CONFIGURED";
                } else {
                    List<EvaluationCriteria> criteriaList = evaluationCriteriaRepository
                            .findByRatioDefinitionRatioCodeAndIsActiveTrue(code);
                    if (!criteriaList.isEmpty()) {
                        EvaluationCriteria ec = criteriaList.get(0);
                        threshold = ec.getThresholdValue();
                        operator = toOperatorSymbol(ec.getComparisonOperator());
                        evalStatus = "NOT_EVALUATED";
                    }
                }

                ratioResponses.add(DashboardRatioResponse.builder()
                        .code(code)
                        .name(name)
                        .category(catCode)
                        .categoryDisplayName(catName)
                        .value(value)
                        .unit(unit)
                        .calculationStatus(calcStatus)
                        .evaluationStatus(evalStatus)
                        .threshold(threshold)
                        .comparisonOperator(operator)
                        .missingDataExplanation(missingExp)
                        .build());
            }

            categories.add(DashboardCategoryResponse.builder()
                    .category(catCode)
                    .displayName(catName)
                    .ratios(ratioResponses)
                    .build());
        }

        return categories;
    }

    private DashboardVisualAnalyticsResponse buildVisualAnalytics(
            Map<String, FinancialCalculationResult> calcMap,
            Map<String, AssessmentDetail> detailMap,
            FinancialStatement statement,
            List<DashboardCategoryResponse> categories
    ) {
        List<RatioComparisonDTO> comparisons = new ArrayList<>();

        for (DashboardCategoryResponse cat : categories) {
            for (DashboardRatioResponse r : cat.getRatios()) {
                comparisons.add(RatioComparisonDTO.builder()
                        .ratioCode(r.getCode())
                        .ratioName(r.getName())
                        .category(r.getCategory())
                        .actualValue(r.getValue())
                        .unit(r.getUnit())
                        .configuredThreshold(r.getThreshold())
                        .comparisonOperator(r.getComparisonOperator())
                        .evaluationStatus(r.getEvaluationStatus())
                        .industryBenchmark(null)
                        .benchmarkStatus("NOT_CONFIGURED")
                        .build());
            }
        }

        CapitalStructureDTO capitalStructure = extractCapitalStructure(statement, calcMap);

        return DashboardVisualAnalyticsResponse.builder()
                .ratioComparisons(comparisons)
                .capitalStructure(capitalStructure)
                .categoryOverview(categories)
                .build();
    }

    private CapitalStructureDTO extractCapitalStructure(
            FinancialStatement statement,
            Map<String, FinancialCalculationResult> calcMap
    ) {
        BigDecimal totalAssets = null;
        BigDecimal totalLiabilities = null;
        BigDecimal equity = null;

        if (statement.getItems() != null) {
            for (FinancialStatementItem item : statement.getItems()) {
                String code = item.getCode() != null ? item.getCode().trim() : "";
                String key = item.getKey() != null ? item.getKey().trim().toUpperCase() : "";

                if (totalAssets == null && ("280".equals(code) || "270".equals(code) || "TOTAL_ASSETS".equals(key))) {
                    totalAssets = item.getCurrentValue();
                } else if (totalLiabilities == null && ("300".equals(code) || "TOTAL_LIABILITIES".equals(key))) {
                    totalLiabilities = item.getCurrentValue();
                } else if (equity == null && ("400".equals(code) || "EQUITY".equals(key))) {
                    equity = item.getCurrentValue();
                }
            }
        }

        FinancialCalculationResult debtRatioCalc = calcMap.get("DEBT_RATIO");
        BigDecimal debtRatio = (debtRatioCalc != null && debtRatioCalc.getStatus() == CalculationStatus.SUCCESS)
                ? debtRatioCalc.getResultValue()
                : null;

        FinancialCalculationResult equityRatioCalc = calcMap.get("EQUITY_RATIO");
        BigDecimal equityRatio = (equityRatioCalc != null && equityRatioCalc.getStatus() == CalculationStatus.SUCCESS)
                ? equityRatioCalc.getResultValue()
                : null;

        return CapitalStructureDTO.builder()
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .equity(equity)
                .unit(statement.getUnit() != null ? statement.getUnit() : "VND")
                .debtRatio(debtRatio)
                .equityRatio(equityRatio)
                .build();
    }

    private DashboardValidationResponse buildValidationResponse(
            ValidationExecution execution,
            FinancialStatement statement
    ) {
        if (execution == null) {
            return buildEmptyValidationResponse("VALIDATION_NOT_AVAILABLE");
        }

        List<ValidationResultItemDTO> resultItems = new ArrayList<>();
        if (execution.getResults() != null) {
            for (ValidationResult vr : execution.getResults()) {
                resultItems.add(ValidationResultItemDTO.builder()
                        .ruleCode(vr.getRuleCodeSnapshot())
                        .ruleName(vr.getRuleNameSnapshot())
                        .category(vr.getCategory() != null ? vr.getCategory().name() : null)
                        .status(vr.getStatus() != null ? vr.getStatus().name() : null)
                        .severity(vr.getSeverity() != null ? vr.getSeverity().name() : null)
                        .expectedValue(vr.getExpectedValue())
                        .actualValue(vr.getActualValue())
                        .differenceValue(vr.getDifferenceValue())
                        .explanation(vr.getExplanation())
                        .sourcePages(vr.getSourcePages())
                        .affectedIndicators(vr.getAffectedIndicators())
                        .build());
            }
        }

        return DashboardValidationResponse.builder()
                .validationId(execution.getId())
                .overallStatus(execution.getExecutionStatus() != null ? execution.getExecutionStatus().name() : "UNKNOWN")
                .accountingStandard(execution.getAccountingStandard() != null ? execution.getAccountingStandard().name() : statement.getAccountingStandard())
                .totalRules(execution.getTotalRules())
                .passedRules(execution.getPassedRules())
                .warningRules(execution.getWarningRules())
                .failedRules(execution.getFailedRules())
                .reviewRequiredRules(execution.getReviewRequiredRules())
                .errorRules(execution.getErrorRules())
                .notApplicableRules(execution.getNotApplicableRules())
                .executedAt(execution.getExecutedAt())
                .results(resultItems)
                .build();
    }

    private DashboardValidationResponse buildEmptyValidationResponse(String status) {
        return DashboardValidationResponse.builder()
                .validationId(null)
                .overallStatus(status)
                .accountingStandard(null)
                .totalRules(0)
                .passedRules(0)
                .warningRules(0)
                .failedRules(0)
                .reviewRequiredRules(0)
                .errorRules(0)
                .notApplicableRules(0)
                .executedAt(null)
                .results(Collections.emptyList())
                .build();
    }

    private DashboardRiskResponse buildRiskResponse(
            RiskAnalysis riskAnalysis,
            RiskSeverity severityFilter,
            RiskCategory categoryFilter,
            RiskStatus statusFilter
    ) {
        if (riskAnalysis == null) {
            return buildEmptyRiskResponse("RISK_ANALYSIS_NOT_AVAILABLE");
        }

        DashboardRiskSummaryDTO summary = DashboardRiskSummaryDTO.builder()
                .totalRisks(riskAnalysis.getTotalRisks())
                .lowRisks(riskAnalysis.getLowRisks())
                .mediumRisks(riskAnalysis.getMediumRisks())
                .highRisks(riskAnalysis.getHighRisks())
                .criticalRisks(riskAnalysis.getCriticalRisks())
                .resolvedRisks(riskAnalysis.getResolvedRisks())
                .notConfiguredCount(riskAnalysis.getNotConfiguredCount())
                .insufficientDataCount(riskAnalysis.getInsufficientDataCount())
                .build();

        List<DashboardIdentifiedRiskDTO> risks = new ArrayList<>();
        if (riskAnalysis.getIdentifiedRisks() != null) {
            for (IdentifiedRisk ir : riskAnalysis.getIdentifiedRisks()) {
                if (severityFilter != null && ir.getSeverity() != severityFilter) {
                    continue;
                }
                if (categoryFilter != null && ir.getRiskCategory() != categoryFilter) {
                    continue;
                }
                if (statusFilter != null && ir.getRiskStatus() != statusFilter) {
                    continue;
                }

                risks.add(DashboardIdentifiedRiskDTO.builder()
                        .riskId(ir.getId())
                        .category(ir.getRiskCategory() != null ? ir.getRiskCategory().name() : null)
                        .title(ir.getRiskTitle())
                        .description(ir.getRiskDescription())
                        .severity(ir.getSeverity() != null ? ir.getSeverity().name() : null)
                        .status(ir.getRiskStatus() != null ? ir.getRiskStatus().name() : null)
                        .evidence(ir.getSourceReference() != null ? ir.getSourceReference() : ir.getPotentialImpact())
                        .relatedRatioCode(ir.getRelatedRatioCode())
                        .actualValue(ir.getActualValue())
                        .thresholdValue(ir.getThresholdValue())
                        .recommendation(ir.getRecommendation())
                        .build());
            }
        }

        return DashboardRiskResponse.builder()
                .riskAnalysisId(riskAnalysis.getId())
                .executionStatus(riskAnalysis.getExecutionStatus() != null ? riskAnalysis.getExecutionStatus().name() : "COMPLETED")
                .summary(summary)
                .identifiedRisks(risks)
                .build();
    }

    private DashboardRiskResponse buildEmptyRiskResponse(String status) {
        DashboardRiskSummaryDTO emptySummary = DashboardRiskSummaryDTO.builder()
                .totalRisks(0)
                .lowRisks(0)
                .mediumRisks(0)
                .highRisks(0)
                .criticalRisks(0)
                .resolvedRisks(0)
                .notConfiguredCount(0)
                .insufficientDataCount(0)
                .build();

        return DashboardRiskResponse.builder()
                .riskAnalysisId(null)
                .executionStatus(status)
                .summary(emptySummary)
                .identifiedRisks(Collections.emptyList())
                .build();
    }

    private DashboardAICommentaryResponse buildAICommentaryResponse(AIAnalysis aiAnalysis) {
        if (aiAnalysis == null) {
            return buildEmptyAICommentaryResponse("AI_ANALYSIS_NOT_AVAILABLE");
        }

        List<DashboardAIStrengthDTO> strengths = new ArrayList<>();
        if (aiAnalysis.getStrengths() != null) {
            for (AIAnalysisStrength s : aiAnalysis.getStrengths()) {
                strengths.add(DashboardAIStrengthDTO.builder()
                        .title(s.getTitle())
                        .description(s.getDescription())
                        .evidence(deserializeEvidence(s.getEvidence()))
                        .build());
            }
        }

        List<DashboardAIRiskDTO> risks = new ArrayList<>();
        if (aiAnalysis.getRisks() != null) {
            for (AIAnalysisRisk r : aiAnalysis.getRisks()) {
                risks.add(DashboardAIRiskDTO.builder()
                        .title(r.getTitle())
                        .description(r.getDescription())
                        .severity(r.getSeverity() != null ? r.getSeverity().name() : null)
                        .category(r.getCategory() != null ? r.getCategory().name() : null)
                        .evidence(deserializeEvidence(r.getEvidence()))
                        .sourceRiskId(r.getSourceRiskId())
                        .build());
            }
        }

        List<DashboardAIRecommendationDTO> recommendations = new ArrayList<>();
        if (aiAnalysis.getRecommendations() != null) {
            for (AIAnalysisRecommendation rec : aiAnalysis.getRecommendations()) {
                recommendations.add(DashboardAIRecommendationDTO.builder()
                        .title(rec.getTitle())
                        .description(rec.getDescription())
                        .priority(rec.getPriority() != null ? rec.getPriority().name() : null)
                        .relatedRiskId(rec.getRelatedRiskId() != null ? rec.getRelatedRiskId() : rec.getRelatedRisk())
                        .build());
            }
        }

        List<String> limitations = new ArrayList<>();
        if (aiAnalysis.getLimitations() != null) {
            for (AIAnalysisLimitation lim : aiAnalysis.getLimitations()) {
                if (lim.getDescription() != null && !lim.getDescription().isBlank()) {
                    limitations.add(lim.getDescription());
                }
            }
        }

        return DashboardAICommentaryResponse.builder()
                .analysisId(aiAnalysis.getId())
                .executiveSummary(aiAnalysis.getExecutiveSummary())
                .strengths(strengths)
                .risks(risks)
                .recommendations(recommendations)
                .limitations(limitations)
                .aiGenerationStatus(aiAnalysis.getStatus() != null ? aiAnalysis.getStatus().name() : null)
                .reviewStatus(aiAnalysis.getReviewStatus() != null ? aiAnalysis.getReviewStatus().name() : null)
                .aiGeneratedComment(aiAnalysis.getAiGeneratedComment())
                .finalComment(aiAnalysis.getFinalComment())
                .generatedAt(aiAnalysis.getCreatedAt())
                .completedAt(aiAnalysis.getCompletedAt())
                .build();
    }

    private DashboardAICommentaryResponse buildEmptyAICommentaryResponse(String status) {
        return DashboardAICommentaryResponse.builder()
                .analysisId(null)
                .executiveSummary(null)
                .strengths(Collections.emptyList())
                .risks(Collections.emptyList())
                .recommendations(Collections.emptyList())
                .limitations(Collections.emptyList())
                .aiGenerationStatus(status)
                .reviewStatus("NOT_AVAILABLE")
                .aiGeneratedComment(null)
                .finalComment(null)
                .generatedAt(null)
                .completedAt(null)
                .build();
    }

    private List<String> deserializeEvidence(String evidenceJson) {
        if (evidenceJson == null || evidenceJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(evidenceJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of(evidenceJson);
        }
    }

    private String toOperatorSymbol(ComparisonOperator operator) {
        if (operator == null) return null;
        return switch (operator) {
            case GREATER_THAN -> ">";
            case GREATER_THAN_OR_EQUAL -> ">=";
            case LESS_THAN -> "<";
            case LESS_THAN_OR_EQUAL -> "<=";
            case EQUAL -> "=";
            case BETWEEN -> "BETWEEN";
        };
    }
}
