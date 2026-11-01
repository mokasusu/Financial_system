package com.financial.modules.financialindicator.mapper;

import com.financial.modules.financialindicator.dto.AccountingIndicatorMappingResponse;
import com.financial.modules.financialindicator.dto.FinancialIndicatorResponse;
import com.financial.modules.financialindicator.dto.FinancialRatioInputResponse;
import com.financial.modules.financialindicator.dto.FinancialRatioResponse;
import com.financial.modules.financialindicator.entity.AccountingIndicatorMapping;
import com.financial.modules.financialindicator.entity.FinancialIndicator;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.entity.FinancialRatioInput;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FinancialIndicatorMapper {

    public FinancialIndicatorResponse toIndicatorResponse(FinancialIndicator indicator) {
        if (indicator == null) {
            return null;
        }
        return FinancialIndicatorResponse.builder()
                .id(indicator.getId())
                .indicatorCode(indicator.getIndicatorCode())
                .indicatorName(indicator.getIndicatorName())
                .indicatorNameEn(indicator.getIndicatorNameEn())
                .statementType(indicator.getStatementType())
                .dataType(indicator.getDataType())
                .unit(indicator.getUnit())
                .description(indicator.getDescription())
                .isActive(indicator.getIsActive())
                .createdAt(indicator.getCreatedAt())
                .updatedAt(indicator.getUpdatedAt())
                .build();
    }

    public List<FinancialIndicatorResponse> toIndicatorResponseList(List<FinancialIndicator> indicators) {
        if (indicators == null) {
            return Collections.emptyList();
        }
        return indicators.stream()
                .map(this::toIndicatorResponse)
                .collect(Collectors.toList());
    }

    public FinancialRatioInputResponse toRatioInputResponse(FinancialRatioInput input) {
        if (input == null) {
            return null;
        }
        return FinancialRatioInputResponse.builder()
                .id(input.getId())
                .ratioDefinitionId(input.getRatioDefinition() != null ? input.getRatioDefinition().getId() : null)
                .ratioCode(input.getRatioDefinition() != null ? input.getRatioDefinition().getRatioCode() : null)
                .indicatorId(input.getIndicator() != null ? input.getIndicator().getId() : null)
                .indicatorCode(input.getIndicator() != null ? input.getIndicator().getIndicatorCode() : null)
                .indicatorName(input.getIndicator() != null ? input.getIndicator().getIndicatorName() : null)
                .inputAlias(input.getInputAlias())
                .inputOrder(input.getInputOrder())
                .required(input.getRequired())
                .periodReference(input.getPeriodReference())
                .build();
    }

    public List<FinancialRatioInputResponse> toRatioInputResponseList(List<FinancialRatioInput> inputs) {
        if (inputs == null) {
            return Collections.emptyList();
        }
        return inputs.stream()
                .map(this::toRatioInputResponse)
                .collect(Collectors.toList());
    }

    public FinancialRatioResponse toRatioResponse(FinancialRatioDefinition ratio) {
        if (ratio == null) {
            return null;
        }
        List<FinancialRatioInputResponse> inputResponses = Collections.emptyList();
        if (ratio.getInputs() != null && !ratio.getInputs().isEmpty()) {
            inputResponses = ratio.getInputs().stream()
                    .map(this::toRatioInputResponse)
                    .collect(Collectors.toList());
        }

        return FinancialRatioResponse.builder()
                .id(ratio.getId())
                .ratioCode(ratio.getRatioCode())
                .ratioName(ratio.getRatioName())
                .ratioNameEn(ratio.getRatioNameEn())
                .category(ratio.getCategory())
                .formulaExpression(ratio.getFormulaExpression())
                .unit(ratio.getUnit())
                .evaluationDirection(ratio.getEvaluationDirection())
                .decimalScale(ratio.getDecimalScale())
                .isCoreKpi(ratio.getIsCoreKpi())
                .description(ratio.getDescription())
                .isActive(ratio.getIsActive())
                .inputs(inputResponses)
                .createdAt(ratio.getCreatedAt())
                .updatedAt(ratio.getUpdatedAt())
                .build();
    }

    public List<FinancialRatioResponse> toRatioResponseList(List<FinancialRatioDefinition> ratios) {
        if (ratios == null) {
            return Collections.emptyList();
        }
        return ratios.stream()
                .map(this::toRatioResponse)
                .collect(Collectors.toList());
    }

    public AccountingIndicatorMappingResponse toMappingResponse(AccountingIndicatorMapping mapping) {
        if (mapping == null) {
            return null;
        }
        return AccountingIndicatorMappingResponse.builder()
                .id(mapping.getId())
                .indicatorId(mapping.getIndicator() != null ? mapping.getIndicator().getId() : null)
                .indicatorCode(mapping.getIndicator() != null ? mapping.getIndicator().getIndicatorCode() : null)
                .indicatorName(mapping.getIndicator() != null ? mapping.getIndicator().getIndicatorName() : null)
                .accountingStandard(mapping.getAccountingStandard())
                .statementType(mapping.getStatementType())
                .sourceCode(mapping.getSourceCode())
                .sourceKey(mapping.getSourceKey())
                .sourceLabel(mapping.getSourceLabel())
                .mappingStatus(mapping.getMappingStatus())
                .effectiveFrom(mapping.getEffectiveFrom())
                .effectiveTo(mapping.getEffectiveTo())
                .build();
    }

    public List<AccountingIndicatorMappingResponse> toMappingResponseList(List<AccountingIndicatorMapping> mappings) {
        if (mappings == null) {
            return Collections.emptyList();
        }
        return mappings.stream()
                .map(this::toMappingResponse)
                .collect(Collectors.toList());
    }
}

