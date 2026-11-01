package com.financial.modules.assessment.service;

import com.financial.modules.assessment.entity.AssessmentCategoryScore;
import com.financial.modules.assessment.entity.AssessmentDetail;
import com.financial.modules.assessment.enums.AssessmentScoringStatus;
import com.financial.modules.assessment.enums.RatioEvaluationStatus;
import com.financial.modules.assessment.mapper.AssessmentMapper;
import com.financial.modules.assessment.service.impl.CategoryAggregationServiceImpl;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryAggregationServiceTest {

    private CategoryAggregationService categoryAggregationService;
    private final AssessmentMapper mapper = new AssessmentMapper();

    @BeforeEach
    void setUp() {
        categoryAggregationService = new CategoryAggregationServiceImpl(mapper);
    }

    @Test
    @DisplayName("Aggregates 14 ratio details into 4 categories correctly")
    void testCategoryAggregation_CountsAndCategories() {
        List<AssessmentDetail> details = new ArrayList<>();

        // Liquidity: 1 PASS, 1 FAIL, 1 NOT_CONFIGURED
        details.add(createDetail("CURRENT_RATIO", FinancialRatioCategory.LIQUIDITY, RatioEvaluationStatus.PASS));
        details.add(createDetail("QUICK_RATIO", FinancialRatioCategory.LIQUIDITY, RatioEvaluationStatus.FAIL));
        details.add(createDetail("CASH_RATIO", FinancialRatioCategory.LIQUIDITY, RatioEvaluationStatus.NOT_CONFIGURED));

        // Solvency: 1 PASS, 2 NOT_CONFIGURED
        details.add(createDetail("DEBT_TO_EQUITY", FinancialRatioCategory.SOLVENCY, RatioEvaluationStatus.PASS));
        details.add(createDetail("DEBT_RATIO", FinancialRatioCategory.SOLVENCY, RatioEvaluationStatus.NOT_CONFIGURED));
        details.add(createDetail("EQUITY_RATIO", FinancialRatioCategory.SOLVENCY, RatioEvaluationStatus.NOT_CONFIGURED));

        // Profitability: 2 PASS, 1 INSUFFICIENT_DATA, 1 NOT_CONFIGURED
        details.add(createDetail("ROA", FinancialRatioCategory.PROFITABILITY, RatioEvaluationStatus.PASS));
        details.add(createDetail("ROE", FinancialRatioCategory.PROFITABILITY, RatioEvaluationStatus.PASS));
        details.add(createDetail("NET_PROFIT_MARGIN", FinancialRatioCategory.PROFITABILITY, RatioEvaluationStatus.INSUFFICIENT_DATA));
        details.add(createDetail("GROSS_PROFIT_MARGIN", FinancialRatioCategory.PROFITABILITY, RatioEvaluationStatus.NOT_CONFIGURED));

        // Efficiency: 4 NOT_CONFIGURED
        details.add(createDetail("ASSET_TURNOVER", FinancialRatioCategory.EFFICIENCY, RatioEvaluationStatus.NOT_CONFIGURED));
        details.add(createDetail("INVENTORY_TURNOVER", FinancialRatioCategory.EFFICIENCY, RatioEvaluationStatus.NOT_CONFIGURED));
        details.add(createDetail("RECEIVABLES_TURNOVER", FinancialRatioCategory.EFFICIENCY, RatioEvaluationStatus.NOT_CONFIGURED));
        details.add(createDetail("PAYABLES_TURNOVER", FinancialRatioCategory.EFFICIENCY, RatioEvaluationStatus.NOT_CONFIGURED));

        List<AssessmentCategoryScore> categoryScores = categoryAggregationService.aggregateCategories(details);

        assertThat(categoryScores).hasSize(4);

        // Liquidity
        AssessmentCategoryScore liq = categoryScores.stream().filter(c -> c.getCategory() == FinancialRatioCategory.LIQUIDITY).findFirst().orElseThrow();
        assertThat(liq.getTotalRatios()).isEqualTo(3);
        assertThat(liq.getPassedRatios()).isEqualTo(1);
        assertThat(liq.getFailedRatios()).isEqualTo(1);
        assertThat(liq.getNotConfiguredRatios()).isEqualTo(1);
        assertThat(liq.getInsufficientDataRatios()).isEqualTo(0);
        assertThat(liq.getCategoryScore()).isNull();
        assertThat(liq.getScoringStatus()).isEqualTo(AssessmentScoringStatus.NOT_CONFIGURED);

        // Solvency
        AssessmentCategoryScore solv = categoryScores.stream().filter(c -> c.getCategory() == FinancialRatioCategory.SOLVENCY).findFirst().orElseThrow();
        assertThat(solv.getTotalRatios()).isEqualTo(3);
        assertThat(solv.getPassedRatios()).isEqualTo(1);
        assertThat(solv.getNotConfiguredRatios()).isEqualTo(2);

        // Profitability
        AssessmentCategoryScore prof = categoryScores.stream().filter(c -> c.getCategory() == FinancialRatioCategory.PROFITABILITY).findFirst().orElseThrow();
        assertThat(prof.getTotalRatios()).isEqualTo(4);
        assertThat(prof.getPassedRatios()).isEqualTo(2);
        assertThat(prof.getInsufficientDataRatios()).isEqualTo(1);
        assertThat(prof.getNotConfiguredRatios()).isEqualTo(1);

        // Efficiency
        AssessmentCategoryScore eff = categoryScores.stream().filter(c -> c.getCategory() == FinancialRatioCategory.EFFICIENCY).findFirst().orElseThrow();
        assertThat(eff.getTotalRatios()).isEqualTo(4);
        assertThat(eff.getNotConfiguredRatios()).isEqualTo(4);
    }

    @Test
    @DisplayName("Empty details list still generates all 4 categories with 0 counts")
    void testCategoryAggregation_EmptyListGenerates4Categories() {
        List<AssessmentCategoryScore> categoryScores = categoryAggregationService.aggregateCategories(List.of());

        assertThat(categoryScores).hasSize(4);
        for (AssessmentCategoryScore score : categoryScores) {
            assertThat(score.getTotalRatios()).isEqualTo(0);
            assertThat(score.getPassedRatios()).isEqualTo(0);
            assertThat(score.getCategoryScore()).isNull();
            assertThat(score.getScoringStatus()).isEqualTo(AssessmentScoringStatus.NOT_CONFIGURED);
        }
    }

    private AssessmentDetail createDetail(String code, FinancialRatioCategory cat, RatioEvaluationStatus status) {
        return AssessmentDetail.builder()
                .ratioCodeSnapshot(code)
                .ratioNameSnapshot(code)
                .category(cat)
                .unit(FinancialUnit.TIMES)
                .calculatedValue(new BigDecimal("1.5000"))
                .evaluationStatus(status)
                .build();
    }
}

