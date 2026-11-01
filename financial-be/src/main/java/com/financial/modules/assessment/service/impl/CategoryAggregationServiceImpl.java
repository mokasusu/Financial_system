package com.financial.modules.assessment.service.impl;

import com.financial.modules.assessment.entity.AssessmentCategoryScore;
import com.financial.modules.assessment.entity.AssessmentDetail;
import com.financial.modules.assessment.enums.AssessmentScoringStatus;
import com.financial.modules.assessment.enums.RatioEvaluationStatus;
import com.financial.modules.assessment.mapper.AssessmentMapper;
import com.financial.modules.assessment.service.CategoryAggregationService;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryAggregationServiceImpl implements CategoryAggregationService {

    private final AssessmentMapper mapper;

    private static final List<FinancialRatioCategory> ALL_CATEGORIES = List.of(
            FinancialRatioCategory.LIQUIDITY,
            FinancialRatioCategory.SOLVENCY,
            FinancialRatioCategory.PROFITABILITY,
            FinancialRatioCategory.EFFICIENCY
    );

    @Override
    public List<AssessmentCategoryScore> aggregateCategories(List<AssessmentDetail> details) {
        log.info("Aggregating {} assessment details by financial ratio categories", details != null ? details.size() : 0);

        Map<FinancialRatioCategory, List<AssessmentDetail>> grouped = new EnumMap<>(FinancialRatioCategory.class);
        for (FinancialRatioCategory cat : ALL_CATEGORIES) {
            grouped.put(cat, new ArrayList<>());
        }

        if (details != null) {
            for (AssessmentDetail d : details) {
                if (d.getCategory() != null && grouped.containsKey(d.getCategory())) {
                    grouped.get(d.getCategory()).add(d);
                }
            }
        }

        List<AssessmentCategoryScore> result = new ArrayList<>();

        for (FinancialRatioCategory cat : ALL_CATEGORIES) {
            List<AssessmentDetail> catDetails = grouped.get(cat);

            int total = catDetails.size();
            int passed = 0;
            int failed = 0;
            int insufficient = 0;
            int notConfigured = 0;

            for (AssessmentDetail d : catDetails) {
                if (d.getEvaluationStatus() == RatioEvaluationStatus.PASS) {
                    passed++;
                } else if (d.getEvaluationStatus() == RatioEvaluationStatus.FAIL) {
                    failed++;
                } else if (d.getEvaluationStatus() == RatioEvaluationStatus.INSUFFICIENT_DATA) {
                    insufficient++;
                } else if (d.getEvaluationStatus() == RatioEvaluationStatus.NOT_CONFIGURED) {
                    notConfigured++;
                }
            }

            AssessmentCategoryScore categoryScore = AssessmentCategoryScore.builder()
                    .category(cat)
                    .categoryName(mapper.getCategoryDisplayName(cat))
                    .totalRatios(total)
                    .passedRatios(passed)
                    .failedRatios(failed)
                    .insufficientDataRatios(insufficient)
                    .notConfiguredRatios(notConfigured)
                    .categoryScore(null) // No approved scoring policy configured
                    .scoringStatus(AssessmentScoringStatus.NOT_CONFIGURED)
                    .build();

            result.add(categoryScore);
        }

        log.debug("Category aggregation produced {} category score summaries", result.size());
        return result;
    }
}

