package com.financial.modules.validation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.validation.enums.ToleranceType;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Validation rule configuration details")
public class ValidationRuleResponse {

    @Schema(description = "Rule ID", example = "1")
    private Long id;

    @Schema(description = "Unique validation rule code", example = "BS_EQ_TT200_01")
    private String ruleCode;

    @Schema(description = "Validation rule name", example = "Cân đối Bảng cân đối kế toán TT200")
    private String ruleName;

    @Schema(description = "Detailed rule description")
    private String description;

    @Schema(description = "Target accounting standard", example = "TT200")
    private AccountingStandard accountingStandard;

    @Schema(description = "Target statement type", example = "BALANCE_SHEET")
    private FinancialStatementType statementType;

    @Schema(description = "Validation category", example = "ACCOUNTING_EQUATION")
    private ValidationCategory category;

    @Schema(description = "Category display name", example = "Phương trình kế toán")
    private String categoryDisplayName;

    @Schema(description = "Severity level", example = "CRITICAL")
    private ValidationSeverity severity;

    @Schema(description = "Severity display name", example = "Nghiêm trọng")
    private String severityDisplayName;

    @Schema(description = "Structured JSON configuration string")
    private String configuration;

    @Schema(description = "Allowed tolerance threshold value", example = "1000.0000")
    private BigDecimal toleranceValue;

    @Schema(description = "Tolerance calculation type", example = "ABSOLUTE")
    private ToleranceType toleranceType;

    @Schema(description = "Whether the rule is currently active", example = "true")
    private Boolean isActive;

    @Schema(description = "Rule creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Rule last updated timestamp")
    private LocalDateTime updatedAt;
}

