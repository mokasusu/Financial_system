package com.financial.modules.calculationengine.mapper;

import com.financial.modules.calculationengine.dto.CalculationHistoryResponse;
import com.financial.modules.calculationengine.dto.CalculationResponse;
import com.financial.modules.calculationengine.dto.CalculationResultResponse;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CalculationMapper {

    public CalculationResponse toResponse(FinancialCalculation calculation) {
        if (calculation == null) {
            return null;
        }

        List<CalculationResultResponse> resultDtos = calculation.getResults() != null
                ? calculation.getResults().stream().map(this::toResultResponse).toList()
                : Collections.emptyList();

        return CalculationResponse.builder()
                .id(calculation.getId())
                .statementId(calculation.getFinancialStatement() != null ? calculation.getFinancialStatement().getId() : null)
                .calculationVersion(calculation.getCalculationVersion())
                .confirmedDataVersion(calculation.getConfirmedDataVersion())
                .status(calculation.getStatus())
                .statusDisplayName(calculation.getStatus() != null ? calculation.getStatus().getDisplayName() : null)
                .calculatedAt(calculation.getCalculatedAt())
                .createdAt(calculation.getCreatedAt())
                .results(resultDtos)
                .build();
    }

    public CalculationResultResponse toResultResponse(FinancialCalculationResult result) {
        if (result == null) {
            return null;
        }

        return CalculationResultResponse.builder()
                .id(result.getId())
                .ratioCode(result.getRatioCode())
                .ratioName(result.getRatioName())
                .category(result.getCategory())
                .resultValue(result.getResultValue())
                .unit(result.getUnit())
                .unitDisplayName(getUnitDisplayName(result.getUnit()))
                .isCoreKpi(result.getIsCoreKpi())
                .status(result.getStatus())
                .statusDisplayName(result.getStatus() != null ? result.getStatus().getDisplayName() : null)
                .formulaSnapshot(result.getFormulaSnapshot())
                .inputSnapshot(result.getInputSnapshot())
                .calculationNote(result.getCalculationNote())
                .createdAt(result.getCreatedAt())
                .build();
    }

    public CalculationHistoryResponse toHistoryResponse(FinancialCalculation calculation) {
        if (calculation == null) {
            return null;
        }

        List<FinancialCalculationResult> results = calculation.getResults() != null
                ? calculation.getResults()
                : Collections.emptyList();

        int total = results.size();
        int success = (int) results.stream()
                .filter(r -> r.getStatus() == CalculationStatus.SUCCESS)
                .count();

        return CalculationHistoryResponse.builder()
                .id(calculation.getId())
                .statementId(calculation.getFinancialStatement() != null ? calculation.getFinancialStatement().getId() : null)
                .calculationVersion(calculation.getCalculationVersion())
                .confirmedDataVersion(calculation.getConfirmedDataVersion())
                .status(calculation.getStatus())
                .statusDisplayName(calculation.getStatus() != null ? calculation.getStatus().getDisplayName() : null)
                .calculatedAt(calculation.getCalculatedAt())
                .createdAt(calculation.getCreatedAt())
                .totalRatios(total)
                .successfulRatios(success)
                .build();
    }

    private String getUnitDisplayName(com.financial.modules.financialindicator.enums.FinancialUnit unit) {
        if (unit == null) return null;
        return switch (unit) {
            case VND -> "VND";
            case PERCENT -> "%";
            case TIMES -> "Lần";
        };
    }
}
