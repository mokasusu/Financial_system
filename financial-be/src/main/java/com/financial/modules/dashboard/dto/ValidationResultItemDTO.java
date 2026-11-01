package com.financial.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Validation rule execution result item")
public class ValidationResultItemDTO {

    @Schema(description = "Validation rule code", example = "RULE_BALANCE_SHEET_EQ")
    private String ruleCode;

    @Schema(description = "Rule descriptive name", example = "Phương trình cân đối kế toán")
    private String ruleName;

    @Schema(description = "Rule category", example = "BALANCE_SHEET")
    private String category;

    @Schema(description = "Rule status", example = "PASS")
    private String status;

    @Schema(description = "Rule severity level", example = "CRITICAL")
    private String severity;

    @Schema(description = "Expected numeric value if equation-based", example = "10000000000")
    private BigDecimal expectedValue;

    @Schema(description = "Actual numeric value found in statement", example = "10000000000")
    private BigDecimal actualValue;

    @Schema(description = "Absolute difference if any", example = "0")
    private BigDecimal differenceValue;

    @Schema(description = "Validation explanation or discrepancy message")
    private String explanation;

    @Schema(description = "Source pages in statement", example = "1, 2")
    private String sourcePages;

    @Schema(description = "Affected financial indicator codes", example = "TOTAL_ASSETS,TOTAL_LIABILITIES,EQUITY")
    private String affectedIndicators;
}

