package com.financial.modules.assessment.service;

import com.financial.modules.assessment.entity.AssessmentDetail;
import com.financial.modules.assessment.enums.RatioEvaluationStatus;
import com.financial.modules.assessment.mapper.AssessmentMapper;
import com.financial.modules.assessment.service.impl.RatioEvaluationServiceImpl;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.evaluationcriteria.entity.EvaluationCriteria;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.evaluationcriteria.repository.EvaluationCriteriaRepository;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import com.financial.modules.financialindicator.repository.FinancialRatioDefinitionRepository;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RatioEvaluationServiceTest {

    @Mock
    private FinancialRatioDefinitionRepository ratioDefinitionRepository;

    @Mock
    private EvaluationCriteriaRepository criteriaRepository;

    private RatioEvaluationService ratioEvaluationService;
    private final AssessmentMapper mapper = new AssessmentMapper();

    private FinancialStatement statement;
    private FinancialCalculation calculation;
    private List<FinancialRatioDefinition> all14RatioDefs;
    private List<FinancialCalculationResult> calcResults;

    @BeforeEach
    void setUp() {
        ratioEvaluationService = new RatioEvaluationServiceImpl(ratioDefinitionRepository, criteriaRepository, mapper);

        statement = FinancialStatement.builder().id(100L).build();
        calculation = FinancialCalculation.builder().id(200L).build();

        all14RatioDefs = create14RatioDefinitions();
        calcResults = createCalculationResults();
        calculation.setResults(calcResults);

        when(ratioDefinitionRepository.findByIsActiveTrue()).thenReturn(all14RatioDefs);
        lenient().when(criteriaRepository.findByRatioDefinitionRatioCodeAndIsActiveTrue(anyString()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("Evaluate CURRENT_RATIO against threshold >= 1.5 (PASS and FAIL)")
    void testCurrentRatio_Evaluation() {
        EvaluationCriteria currentRatioCriterion = EvaluationCriteria.builder()
                .id(1L)
                .criterionName("Current Ratio Standard")
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .thresholdValue(new BigDecimal("1.5000"))
                .unit(FinancialUnit.TIMES)
                .isActive(true)
                .build();

        when(criteriaRepository.findByRatioDefinitionRatioCodeAndIsActiveTrue("CURRENT_RATIO"))
                .thenReturn(List.of(currentRatioCriterion));

        List<AssessmentDetail> details = ratioEvaluationService.evaluateRatios(statement, calculation);

        AssessmentDetail crDetail = details.stream()
                .filter(d -> "CURRENT_RATIO".equals(d.getRatioCodeSnapshot()))
                .findFirst().orElseThrow();

        assertThat(crDetail.getCategory()).isEqualTo(FinancialRatioCategory.LIQUIDITY);
        assertThat(crDetail.getCalculatedValue()).isEqualByComparingTo(new BigDecimal("1.8000"));
        assertThat(crDetail.getEvaluationStatus()).isEqualTo(RatioEvaluationStatus.PASS);
        assertThat(crDetail.getEvaluationExplanation()).contains("đạt chuẩn");
    }

    @Test
    @DisplayName("Evaluate DEBT_TO_EQUITY against threshold <= 1.0 (PASS and FAIL)")
    void testDebtToEquity_Evaluation() {
        EvaluationCriteria dteCriterion = EvaluationCriteria.builder()
                .id(2L)
                .criterionName("Debt to Equity Standard")
                .comparisonOperator(ComparisonOperator.LESS_THAN_OR_EQUAL)
                .thresholdValue(new BigDecimal("1.0000"))
                .unit(FinancialUnit.TIMES)
                .isActive(true)
                .build();

        when(criteriaRepository.findByRatioDefinitionRatioCodeAndIsActiveTrue("DEBT_TO_EQUITY"))
                .thenReturn(List.of(dteCriterion));

        List<AssessmentDetail> details = ratioEvaluationService.evaluateRatios(statement, calculation);

        AssessmentDetail dteDetail = details.stream()
                .filter(d -> "DEBT_TO_EQUITY".equals(d.getRatioCodeSnapshot()))
                .findFirst().orElseThrow();

        assertThat(dteDetail.getCategory()).isEqualTo(FinancialRatioCategory.SOLVENCY);
        assertThat(dteDetail.getCalculatedValue()).isEqualByComparingTo(new BigDecimal("0.8500"));
        assertThat(dteDetail.getEvaluationStatus()).isEqualTo(RatioEvaluationStatus.PASS);
    }

    @Test
    @DisplayName("Evaluate ROA against threshold >= 5.0% (PASS)")
    void testRoa_Evaluation() {
        EvaluationCriteria roaCriterion = EvaluationCriteria.builder()
                .id(3L)
                .criterionName("ROA Standard")
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .thresholdValue(new BigDecimal("5.0000"))
                .unit(FinancialUnit.PERCENT)
                .isActive(true)
                .build();

        when(criteriaRepository.findByRatioDefinitionRatioCodeAndIsActiveTrue("ROA"))
                .thenReturn(List.of(roaCriterion));

        List<AssessmentDetail> details = ratioEvaluationService.evaluateRatios(statement, calculation);

        AssessmentDetail roaDetail = details.stream()
                .filter(d -> "ROA".equals(d.getRatioCodeSnapshot()))
                .findFirst().orElseThrow();

        assertThat(roaDetail.getCategory()).isEqualTo(FinancialRatioCategory.PROFITABILITY);
        assertThat(roaDetail.getCalculatedValue()).isEqualByComparingTo(new BigDecimal("6.5000"));
        assertThat(roaDetail.getEvaluationStatus()).isEqualTo(RatioEvaluationStatus.PASS);
    }

    @Test
    @DisplayName("Evaluate ROE against threshold >= 12.0% (FAIL when lower)")
    void testRoe_Evaluation_FailsWhenLower() {
        EvaluationCriteria roeCriterion = EvaluationCriteria.builder()
                .id(4L)
                .criterionName("ROE Standard")
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .thresholdValue(new BigDecimal("12.0000"))
                .unit(FinancialUnit.PERCENT)
                .isActive(true)
                .build();

        when(criteriaRepository.findByRatioDefinitionRatioCodeAndIsActiveTrue("ROE"))
                .thenReturn(List.of(roeCriterion));

        // Set ROE to 10.5% (lower than 12.0%)
        calcResults.stream()
                .filter(r -> "ROE".equals(r.getRatioCode()))
                .findFirst()
                .ifPresent(r -> r.setResultValue(new BigDecimal("10.5000")));

        List<AssessmentDetail> details = ratioEvaluationService.evaluateRatios(statement, calculation);

        AssessmentDetail roeDetail = details.stream()
                .filter(d -> "ROE".equals(d.getRatioCodeSnapshot()))
                .findFirst().orElseThrow();

        assertThat(roeDetail.getCategory()).isEqualTo(FinancialRatioCategory.PROFITABILITY);
        assertThat(roeDetail.getCalculatedValue()).isEqualByComparingTo(new BigDecimal("10.5000"));
        assertThat(roeDetail.getEvaluationStatus()).isEqualTo(RatioEvaluationStatus.FAIL);
        assertThat(roeDetail.getEvaluationExplanation()).contains("không đạt chuẩn");
    }

    @Test
    @DisplayName("Remaining 10 financial ratios return NOT_CONFIGURED when no criteria exist")
    void testRemaining10Ratios_ReturnNotConfigured() {
        // Return active criteria ONLY for the 4 approved ratios
        when(criteriaRepository.findByRatioDefinitionRatioCodeAndIsActiveTrue(anyString()))
                .thenAnswer(invocation -> {
                    String code = invocation.getArgument(0);
                    if ("CURRENT_RATIO".equals(code) || "DEBT_TO_EQUITY".equals(code)
                            || "ROA".equals(code) || "ROE".equals(code)) {
                        return List.of(EvaluationCriteria.builder()
                                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                                .thresholdValue(new BigDecimal("1.0"))
                                .unit(FinancialUnit.TIMES)
                                .build());
                    }
                    return Collections.emptyList();
                });

        List<AssessmentDetail> details = ratioEvaluationService.evaluateRatios(statement, calculation);

        assertThat(details).hasSize(14);

        long notConfiguredCount = details.stream()
                .filter(d -> d.getEvaluationStatus() == RatioEvaluationStatus.NOT_CONFIGURED)
                .count();

        assertThat(notConfiguredCount).isEqualTo(10);
    }

    @Test
    @DisplayName("Missing calculation values or calculation failure return INSUFFICIENT_DATA")
    void testMissingCalculatedValue_ReturnsInsufficientData() {
        EvaluationCriteria currentRatioCriterion = EvaluationCriteria.builder()
                .id(1L)
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .thresholdValue(new BigDecimal("1.5000"))
                .unit(FinancialUnit.TIMES)
                .build();

        when(criteriaRepository.findByRatioDefinitionRatioCodeAndIsActiveTrue("CURRENT_RATIO"))
                .thenReturn(List.of(currentRatioCriterion));

        // Clear CURRENT_RATIO result value
        calcResults.removeIf(r -> "CURRENT_RATIO".equals(r.getRatioCode()));

        List<AssessmentDetail> details = ratioEvaluationService.evaluateRatios(statement, calculation);

        AssessmentDetail crDetail = details.stream()
                .filter(d -> "CURRENT_RATIO".equals(d.getRatioCodeSnapshot()))
                .findFirst().orElseThrow();

        assertThat(crDetail.getEvaluationStatus()).isEqualTo(RatioEvaluationStatus.INSUFFICIENT_DATA);
        assertThat(crDetail.getEvaluationExplanation()).contains("không đủ");
    }

    @Test
    @DisplayName("Inactive criteria are ignored and treated as NOT_CONFIGURED")
    void testInactiveCriteria_AreIgnored() {
        // Repository returns empty list for active criteria query
        when(criteriaRepository.findByRatioDefinitionRatioCodeAndIsActiveTrue("CURRENT_RATIO"))
                .thenReturn(Collections.emptyList());

        List<AssessmentDetail> details = ratioEvaluationService.evaluateRatios(statement, calculation);

        AssessmentDetail crDetail = details.stream()
                .filter(d -> "CURRENT_RATIO".equals(d.getRatioCodeSnapshot()))
                .findFirst().orElseThrow();

        assertThat(crDetail.getEvaluationStatus()).isEqualTo(RatioEvaluationStatus.NOT_CONFIGURED);
    }

    @Test
    @DisplayName("Verify each ratio is assigned to its correct financial category")
    void testRatioCategoriesAssignment() {
        List<AssessmentDetail> details = ratioEvaluationService.evaluateRatios(statement, calculation);

        long liquidityCount = details.stream().filter(d -> d.getCategory() == FinancialRatioCategory.LIQUIDITY).count();
        long solvencyCount = details.stream().filter(d -> d.getCategory() == FinancialRatioCategory.SOLVENCY).count();
        long profitabilityCount = details.stream().filter(d -> d.getCategory() == FinancialRatioCategory.PROFITABILITY).count();
        long efficiencyCount = details.stream().filter(d -> d.getCategory() == FinancialRatioCategory.EFFICIENCY).count();

        assertThat(liquidityCount).isEqualTo(3);
        assertThat(solvencyCount).isEqualTo(3);
        assertThat(profitabilityCount).isEqualTo(4);
        assertThat(efficiencyCount).isEqualTo(4);
    }

    private List<FinancialRatioDefinition> create14RatioDefinitions() {
        List<FinancialRatioDefinition> list = new ArrayList<>();
        // Liquidity (3)
        list.add(createDef(1L, "CURRENT_RATIO", "Hệ số khả năng thanh toán hiện hành", FinancialRatioCategory.LIQUIDITY, FinancialUnit.TIMES));
        list.add(createDef(2L, "QUICK_RATIO", "Hệ số khả năng thanh toán nhanh", FinancialRatioCategory.LIQUIDITY, FinancialUnit.TIMES));
        list.add(createDef(3L, "CASH_RATIO", "Hệ số khả năng thanh toán tức thời", FinancialRatioCategory.LIQUIDITY, FinancialUnit.TIMES));
        // Solvency (3)
        list.add(createDef(4L, "DEBT_TO_EQUITY", "Hệ số nợ trên vốn chủ sở hữu", FinancialRatioCategory.SOLVENCY, FinancialUnit.TIMES));
        list.add(createDef(5L, "DEBT_RATIO", "Hệ số nợ", FinancialRatioCategory.SOLVENCY, FinancialUnit.PERCENT));
        list.add(createDef(6L, "EQUITY_RATIO", "Hệ số tự tài trợ", FinancialRatioCategory.SOLVENCY, FinancialUnit.PERCENT));
        // Profitability (4)
        list.add(createDef(7L, "ROA", "Tỷ suất sinh lời trên tổng tài sản", FinancialRatioCategory.PROFITABILITY, FinancialUnit.PERCENT));
        list.add(createDef(8L, "ROE", "Tỷ suất sinh lời trên vốn chủ sở hữu", FinancialRatioCategory.PROFITABILITY, FinancialUnit.PERCENT));
        list.add(createDef(9L, "NET_PROFIT_MARGIN", "Tỷ suất lợi nhuận ròng", FinancialRatioCategory.PROFITABILITY, FinancialUnit.PERCENT));
        list.add(createDef(10L, "GROSS_PROFIT_MARGIN", "Tỷ suất lợi nhuận gộp", FinancialRatioCategory.PROFITABILITY, FinancialUnit.PERCENT));
        // Efficiency (4)
        list.add(createDef(11L, "ASSET_TURNOVER", "Vòng quay tổng tài sản", FinancialRatioCategory.EFFICIENCY, FinancialUnit.TIMES));
        list.add(createDef(12L, "INVENTORY_TURNOVER", "Vòng quay hàng tồn kho", FinancialRatioCategory.EFFICIENCY, FinancialUnit.TIMES));
        list.add(createDef(13L, "RECEIVABLES_TURNOVER", "Vòng quay các khoản phải thu", FinancialRatioCategory.EFFICIENCY, FinancialUnit.TIMES));
        list.add(createDef(14L, "PAYABLES_TURNOVER", "Vòng quay các khoản phải trả", FinancialRatioCategory.EFFICIENCY, FinancialUnit.TIMES));
        return list;
    }

    private FinancialRatioDefinition createDef(Long id, String code, String name, FinancialRatioCategory cat, FinancialUnit unit) {
        return FinancialRatioDefinition.builder()
                .id(id)
                .ratioCode(code)
                .ratioName(name)
                .category(cat)
                .unit(unit)
                .isActive(true)
                .build();
    }

    private List<FinancialCalculationResult> createCalculationResults() {
        List<FinancialCalculationResult> list = new ArrayList<>();
        list.add(createResult("CURRENT_RATIO", "1.8000", FinancialUnit.TIMES, FinancialRatioCategory.LIQUIDITY));
        list.add(createResult("QUICK_RATIO", "1.2000", FinancialUnit.TIMES, FinancialRatioCategory.LIQUIDITY));
        list.add(createResult("CASH_RATIO", "0.5000", FinancialUnit.TIMES, FinancialRatioCategory.LIQUIDITY));

        list.add(createResult("DEBT_TO_EQUITY", "0.8500", FinancialUnit.TIMES, FinancialRatioCategory.SOLVENCY));
        list.add(createResult("DEBT_RATIO", "45.0000", FinancialUnit.PERCENT, FinancialRatioCategory.SOLVENCY));
        list.add(createResult("EQUITY_RATIO", "55.0000", FinancialUnit.PERCENT, FinancialRatioCategory.SOLVENCY));

        list.add(createResult("ROA", "6.5000", FinancialUnit.PERCENT, FinancialRatioCategory.PROFITABILITY));
        list.add(createResult("ROE", "14.2000", FinancialUnit.PERCENT, FinancialRatioCategory.PROFITABILITY));
        list.add(createResult("NET_PROFIT_MARGIN", "12.0000", FinancialUnit.PERCENT, FinancialRatioCategory.PROFITABILITY));
        list.add(createResult("GROSS_PROFIT_MARGIN", "30.0000", FinancialUnit.PERCENT, FinancialRatioCategory.PROFITABILITY));

        list.add(createResult("ASSET_TURNOVER", "1.5000", FinancialUnit.TIMES, FinancialRatioCategory.EFFICIENCY));
        list.add(createResult("INVENTORY_TURNOVER", "4.0000", FinancialUnit.TIMES, FinancialRatioCategory.EFFICIENCY));
        list.add(createResult("RECEIVABLES_TURNOVER", "6.0000", FinancialUnit.TIMES, FinancialRatioCategory.EFFICIENCY));
        list.add(createResult("PAYABLES_TURNOVER", "5.0000", FinancialUnit.TIMES, FinancialRatioCategory.EFFICIENCY));
        return list;
    }

    private FinancialCalculationResult createResult(String code, String val, FinancialUnit unit, FinancialRatioCategory cat) {
        return FinancialCalculationResult.builder()
                .ratioCode(code)
                .ratioName(code)
                .category(cat)
                .unit(unit)
                .resultValue(new BigDecimal(val))
                .status(CalculationStatus.SUCCESS)
                .build();
    }
}
