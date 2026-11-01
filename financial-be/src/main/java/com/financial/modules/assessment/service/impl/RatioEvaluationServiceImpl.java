package com.financial.modules.assessment.service.impl;

import com.financial.modules.assessment.entity.AssessmentDetail;
import com.financial.modules.assessment.enums.RatioEvaluationStatus;
import com.financial.modules.assessment.mapper.AssessmentMapper;
import com.financial.modules.assessment.service.RatioEvaluationService;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.evaluationcriteria.entity.EvaluationCriteria;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.evaluationcriteria.repository.EvaluationCriteriaRepository;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.repository.FinancialRatioDefinitionRepository;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatioEvaluationServiceImpl implements RatioEvaluationService {

    private final FinancialRatioDefinitionRepository ratioDefinitionRepository;
    private final EvaluationCriteriaRepository criteriaRepository;
    private final AssessmentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentDetail> evaluateRatios(FinancialStatement statement, FinancialCalculation calculation) {
        log.info("Evaluating financial ratios for statement ID {} using calculation ID {}",
                statement.getId(), calculation != null ? calculation.getId() : "null");

        List<FinancialRatioDefinition> ratioDefs = ratioDefinitionRepository.findByIsActiveTrue();
        if (ratioDefs.isEmpty()) {
            log.warn("No active financial ratio definitions found in the system.");
            return Collections.emptyList();
        }

        Map<String, FinancialCalculationResult> calcResultMap = (calculation != null && calculation.getResults() != null)
                ? calculation.getResults().stream()
                .filter(r -> r.getRatioCode() != null)
                .collect(Collectors.toMap(
                        FinancialCalculationResult::getRatioCode,
                        Function.identity(),
                        (existing, replacement) -> existing
                ))
                : Collections.emptyMap();

        List<AssessmentDetail> details = new ArrayList<>();

        for (FinancialRatioDefinition ratioDef : ratioDefs) {
            String ratioCode = ratioDef.getRatioCode();
            FinancialCalculationResult calcResult = calcResultMap.get(ratioCode);

            List<EvaluationCriteria> criteriaList = criteriaRepository.findByRatioDefinitionRatioCodeAndIsActiveTrue(ratioCode);
            EvaluationCriteria criterion = (!criteriaList.isEmpty()) ? criteriaList.get(0) : null;

            RatioEvaluationStatus evalStatus;
            String explanation;

            if (criterion == null) {
                evalStatus = RatioEvaluationStatus.NOT_CONFIGURED;
                explanation = String.format("Chưa có tiêu chuẩn đánh giá được cấu hình cho chỉ số '%s'.", ratioDef.getRatioName());
            } else if (calcResult == null || calcResult.getResultValue() == null || calcResult.getStatus() != CalculationStatus.SUCCESS) {
                evalStatus = RatioEvaluationStatus.INSUFFICIENT_DATA;
                explanation = String.format("Dữ liệu tính toán không đủ hoặc không khả dụng để đánh giá chỉ số '%s'.", ratioDef.getRatioName());
            } else {
                BigDecimal actualValue = calcResult.getResultValue();
                boolean isPassed = evaluateCondition(actualValue, criterion);

                evalStatus = isPassed ? RatioEvaluationStatus.PASS : RatioEvaluationStatus.FAIL;
                explanation = buildEvaluationExplanation(actualValue, criterion, ratioDef, isPassed);
            }

            AssessmentDetail detail = AssessmentDetail.builder()
                    .ratioDefinition(ratioDef)
                    .ratioCodeSnapshot(ratioDef.getRatioCode())
                    .ratioNameSnapshot(ratioDef.getRatioName())
                    .category(ratioDef.getCategory())
                    .calculatedValue(calcResult != null ? calcResult.getResultValue() : null)
                    .unit(ratioDef.getUnit())
                    .criterion(criterion)
                    .criterionSnapshot(criterion != null ? criterion.getCriterionName() : null)
                    .comparisonOperatorSnapshot(criterion != null ? criterion.getComparisonOperator() : null)
                    .thresholdValueSnapshot(criterion != null ? criterion.getThresholdValue() : null)
                    .minimumValueSnapshot(criterion != null ? criterion.getMinimumValue() : null)
                    .maximumValueSnapshot(criterion != null ? criterion.getMaximumValue() : null)
                    .evaluationStatus(evalStatus)
                    .evaluationExplanation(explanation)
                    .build();

            details.add(detail);
        }

        log.debug("Evaluated {} ratio definitions for statement ID {}", details.size(), statement.getId());
        return details;
    }

    private boolean evaluateCondition(BigDecimal actual, EvaluationCriteria criterion) {
        ComparisonOperator op = criterion.getComparisonOperator();
        BigDecimal threshold = criterion.getThresholdValue();

        if (op == null) {
            return false;
        }

        return switch (op) {
            case GREATER_THAN -> threshold != null && actual.compareTo(threshold) > 0;
            case GREATER_THAN_OR_EQUAL -> threshold != null && actual.compareTo(threshold) >= 0;
            case LESS_THAN -> threshold != null && actual.compareTo(threshold) < 0;
            case LESS_THAN_OR_EQUAL -> threshold != null && actual.compareTo(threshold) <= 0;
            case EQUAL -> threshold != null && actual.compareTo(threshold) == 0;
            case BETWEEN -> {
                BigDecimal min = criterion.getMinimumValue();
                BigDecimal max = criterion.getMaximumValue();
                if (min != null && max != null) {
                    yield actual.compareTo(min) >= 0 && actual.compareTo(max) <= 0;
                } else if (threshold != null) {
                    yield actual.compareTo(threshold) == 0;
                }
                yield false;
            }
        };
    }

    private String buildEvaluationExplanation(
            BigDecimal actual,
            EvaluationCriteria criterion,
            FinancialRatioDefinition ratioDef,
            boolean isPassed
    ) {
        String unitStr = mapper.getUnitDisplayName(criterion.getUnit());
        String opSymbol = mapper.getOperatorSymbol(criterion.getComparisonOperator());

        String targetStr;
        if (criterion.getComparisonOperator() == ComparisonOperator.BETWEEN) {
            targetStr = String.format("[%s - %s] %s",
                    criterion.getMinimumValue() != null ? criterion.getMinimumValue().toPlainString() : "N/A",
                    criterion.getMaximumValue() != null ? criterion.getMaximumValue().toPlainString() : "N/A",
                    unitStr);
        } else {
            targetStr = String.format("%s %s %s",
                    opSymbol != null ? opSymbol : "",
                    criterion.getThresholdValue() != null ? criterion.getThresholdValue().toPlainString() : "N/A",
                    unitStr);
        }

        if (isPassed) {
            return String.format("Chỉ số %s đạt chuẩn (%s %s)",
                    ratioDef.getRatioName(), actual.toPlainString(), targetStr);
        } else {
            return String.format("Chỉ số %s không đạt chuẩn (%s so với yêu cầu %s)",
                    ratioDef.getRatioName(), actual.toPlainString(), targetStr);
        }
    }
}

