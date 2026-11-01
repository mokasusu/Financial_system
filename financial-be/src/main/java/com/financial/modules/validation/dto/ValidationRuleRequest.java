package com.financial.modules.validation.dto;

import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.validation.enums.ToleranceType;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Validation rule creation and update payload")
public class ValidationRuleRequest {

    @NotBlank(message = "Rule code is required")
    @Schema(description = "Unique validation rule code", example = "BS_EQ_TT200_01")
    private String ruleCode;

    @NotBlank(message = "Rule name is required")
    @Schema(description = "Validation rule name", example = "Cân đối Bảng cân đối kế toán TT200")
    private String ruleName;

    @Schema(description = "Detailed rule description")
    private String description;

    @Schema(description = "Target accounting standard (null applies universally)", example = "TT200")
    private AccountingStandard accountingStandard;

    @Schema(description = "Target statement type (null applies across statements)", example = "BALANCE_SHEET")
    private FinancialStatementType statementType;

    @NotNull(message = "Category is required")
    @Schema(description = "Validation category", example = "ACCOUNTING_EQUATION")
    private ValidationCategory category;

    @NotNull(message = "Severity is required")
    @Schema(description = "Severity level", example = "CRITICAL")
    private ValidationSeverity severity;

    @Schema(description = "Structured JSON configuration string specifying parameters")
    private String configuration;

    @Schema(description = "Allowed tolerance threshold value", example = "1000.0000")
    private BigDecimal toleranceValue;

    @Schema(description = "Tolerance calculation type (ABSOLUTE or PERCENTAGE)", example = "ABSOLUTE")
    private ToleranceType toleranceType;

    @Schema(description = "Whether the rule is active", example = "true")
    private Boolean isActive;
}

