package com.financial.modules.riskanalysis.dto;

import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
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
@Schema(description = "Financial risk rule definition response")
public class RiskRuleResponse {

    @Schema(description = "Risk rule ID", example = "1")
    private Long id;

    @Schema(description = "Unique rule code", example = "RISK_LIQ_CURRENT_RATIO_LOW")
    private String ruleCode;

    @Schema(description = "Rule name", example = "Rủi ro mất cân đối thanh toán ngắn hạn")
    private String ruleName;

    @Schema(description = "Rule description", example = "Cảnh báo khi hệ số thanh toán hiện hành xuống dưới 1.0 lần")
    private String description;

    @Schema(description = "Risk category", example = "LIQUIDITY")
    private RiskCategory riskCategory;

    @Schema(description = "Display name of risk category", example = "Rủi ro thanh khoản")
    private String riskCategoryDisplayName;

    @Schema(description = "Target financial ratio code", example = "CURRENT_RATIO")
    private String ratioCode;

    @Schema(description = "Comparison operator", example = "LESS_THAN")
    private ComparisonOperator comparisonOperator;

    @Schema(description = "Symbol of comparison operator", example = "<")
    private String comparisonOperatorSymbol;

    @Schema(description = "Configured threshold value", example = "1.0000")
    private BigDecimal thresholdValue;

    @Schema(description = "Minimum value for range evaluation", example = "null")
    private BigDecimal minimumValue;

    @Schema(description = "Maximum value for range evaluation", example = "null")
    private BigDecimal maximumValue;

    @Schema(description = "Severity classification", example = "HIGH")
    private RiskSeverity severity;

    @Schema(description = "Display name of severity level", example = "Cao")
    private String severityDisplayName;

    @Schema(description = "Explanation template", example = "Chỉ số {ratioName} ở mức {actualValue}...")
    private String explanationTemplate;

    @Schema(description = "Recommendation template", example = "Cần tăng cường quản trị vốn lưu động...")
    private String recommendationTemplate;

    @Schema(description = "Potential impact template", example = "Có nguy cơ thiếu hụt dòng tiền...")
    private String potentialImpactTemplate;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated timestamp")
    private LocalDateTime updatedAt;
}

