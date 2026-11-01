package com.financial.modules.validation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationSeverity;
import com.financial.modules.validation.enums.ValidationStatus;
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
@Schema(description = "Validation execution detailed result for a single rule")
public class ValidationResultResponse {

    @Schema(description = "Validation result ID", example = "1")
    private Long id;

    @Schema(description = "Rule code snapshot", example = "BS_EQ_TT200_01")
    private String ruleCode;

    @Schema(description = "Rule name snapshot", example = "Cân đối Bảng cân đối kế toán TT200")
    private String ruleName;

    @Schema(description = "Validation category", example = "ACCOUNTING_EQUATION")
    private ValidationCategory category;

    @Schema(description = "Category display name", example = "Phương trình kế toán")
    private String categoryDisplayName;

    @Schema(description = "Validation outcome status", example = "PASS")
    private ValidationStatus status;

    @Schema(description = "Status display name", example = "Đạt")
    private String statusDisplayName;

    @Schema(description = "Rule severity level", example = "CRITICAL")
    private ValidationSeverity severity;

    @Schema(description = "Severity display name", example = "Nghiêm trọng")
    private String severityDisplayName;

    @Schema(description = "Expected benchmark value", example = "100000000000.0000")
    private BigDecimal expectedValue;

    @Schema(description = "Actual calculated/reported value", example = "100000000000.0000")
    private BigDecimal actualValue;

    @Schema(description = "Absolute difference amount", example = "0.0000")
    private BigDecimal differenceValue;

    @Schema(description = "Allowed tolerance threshold", example = "1000.0000")
    private BigDecimal toleranceValue;

    @Schema(description = "Detailed outcome explanation", example = "Tổng tài sản bằng Tổng nợ phải trả + Vốn chủ sở hữu")
    private String explanation;

    @Schema(description = "Comma-separated list of affected financial indicator codes", example = "TOTAL_ASSETS,TOTAL_LIABILITIES,EQUITY")
    private String affectedIndicators;

    @Schema(description = "Source page references in financial document", example = "1,2")
    private String sourcePages;

    @Schema(description = "Result creation timestamp")
    private LocalDateTime createdAt;
}

