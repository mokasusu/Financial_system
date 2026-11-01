package com.financial.modules.riskanalysis.dto;

import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
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
@Schema(description = "Request payload for creating or updating a financial risk rule")
public class RiskRuleRequest {

    @NotBlank(message = "Rule code is required")
    @Schema(description = "Unique alphanumeric rule code", example = "RISK_LIQ_CURRENT_RATIO_LOW")
    private String ruleCode;

    @NotBlank(message = "Rule name is required")
    @Schema(description = "Descriptive rule name", example = "Rủi ro mất cân đối thanh toán ngắn hạn")
    private String ruleName;

    @Schema(description = "Detailed description of the risk trigger condition and context", example = "Cảnh báo khi hệ số thanh toán hiện hành xuống dưới mức an toàn")
    private String description;

    @NotNull(message = "Risk category is required")
    @Schema(description = "Financial risk category", example = "LIQUIDITY")
    private RiskCategory riskCategory;

    @Schema(description = "Target financial ratio code to evaluate (if ratio-based)", example = "CURRENT_RATIO")
    private String ratioCode;

    @NotNull(message = "Comparison operator is required")
    @Schema(description = "Comparison operator that triggers the risk when condition is TRUE", example = "LESS_THAN")
    private ComparisonOperator comparisonOperator;

    @Schema(description = "Primary threshold value", example = "1.0000")
    private BigDecimal thresholdValue;

    @Schema(description = "Minimum value for range-based rule", example = "null")
    private BigDecimal minimumValue;

    @Schema(description = "Maximum value for range-based rule", example = "null")
    private BigDecimal maximumValue;

    @NotNull(message = "Severity level is required")
    @Schema(description = "Severity classification when risk is triggered", example = "HIGH")
    private RiskSeverity severity;

    @NotBlank(message = "Explanation template is required")
    @Schema(description = "Template for explanation. Supports variables: {ratioCode}, {ratioName}, {actualValue}, {thresholdValue}, {comparisonOperator}",
            example = "Chỉ số {ratioName} ở mức {actualValue}, vi phạm ngưỡng an toàn {comparisonOperator} {thresholdValue}.")
    private String explanationTemplate;

    @NotBlank(message = "Recommendation template is required")
    @Schema(description = "Actionable mitigation recommendation template",
            example = "Cần tăng cường quản trị vốn lưu động, tái cấu trúc các khoản nợ ngắn hạn và tối ưu hóa vòng quay tiền mặt.")
    private String recommendationTemplate;

    @Schema(description = "Potential consequences template", example = "Có nguy cơ thiếu hụt dòng tiền thanh toán các nghĩa vụ đến hạn trong 3-6 tháng tới.")
    private String potentialImpactTemplate;

    @Schema(description = "Whether the risk rule is actively evaluated", example = "true")
    @Builder.Default
    private Boolean isActive = true;
}

