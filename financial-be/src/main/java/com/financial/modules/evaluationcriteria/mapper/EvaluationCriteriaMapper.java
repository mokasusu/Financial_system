package com.financial.modules.evaluationcriteria.mapper;

import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaResponse;
import com.financial.modules.evaluationcriteria.entity.EvaluationCriteria;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EvaluationCriteriaMapper {

    public EvaluationCriteriaResponse toResponse(EvaluationCriteria entity) {
        if (entity == null) {
            return null;
        }

        return EvaluationCriteriaResponse.builder()
                .id(entity.getId())
                .ratioDefinitionId(entity.getRatioDefinition() != null ? entity.getRatioDefinition().getId() : null)
                .ratioCode(entity.getRatioDefinition() != null ? entity.getRatioDefinition().getRatioCode() : null)
                .ratioName(entity.getRatioDefinition() != null ? entity.getRatioDefinition().getRatioName() : null)
                .criterionName(entity.getCriterionName())
                .comparisonOperator(entity.getComparisonOperator())
                .thresholdValue(entity.getThresholdValue())
                .minimumValue(entity.getMinimumValue())
                .maximumValue(entity.getMaximumValue())
                .unit(entity.getUnit())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<EvaluationCriteriaResponse> toResponseList(List<EvaluationCriteria> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}

