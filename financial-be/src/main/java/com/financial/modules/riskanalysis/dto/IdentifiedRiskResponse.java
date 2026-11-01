package com.financial.modules.riskanalysis.dto;

import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
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
@Schema(description = "Individual identified financial risk item details")
public class IdentifiedRiskResponse {

    @Schema(description = "Identified risk ID", example = "101")
    private Long id;

    @Schema(description = "Triggering risk rule code snapshot", example = "RISK_LIQ_CURRENT_RATIO_LOW")
    private String ruleCode;

    @Schema(description = "Descriptive risk title", example = "Rủi ro mất cân đối thanh toán ngắn hạn")
    private String riskTitle;

    @Schema(description = "Detailed risk explanation based on actual values and trigger conditions",
            example = "Chỉ số Hệ số khả năng thanh toán hiện hành ở mức 0.8500, vi phạm ngưỡng an toàn < 1.0000.")
    private String riskDescription;

    @Schema(description = "Risk category", example = "LIQUIDITY")
    private RiskCategory riskCategory;

    @Schema(description = "Display name of risk category", example = "Rủi ro thanh khoản")
    private String riskCategoryDisplayName;

    @Schema(description = "Severity level (LOW, MEDIUM, HIGH, CRITICAL)", example = "HIGH")
    private RiskSeverity severity;

    @Schema(description = "Display name of severity level", example = "Cao")
    private String severityDisplayName;

    @Schema(description = "Risk lifecycle status (DETECTED, MONITORING, RESOLVED, NOT_CONFIGURED, INSUFFICIENT_DATA)", example = "DETECTED")
    private RiskStatus riskStatus;

    @Schema(description = "Display name of risk status", example = "Phát hiện")
    private String riskStatusDisplayName;

    @Schema(description = "Related financial ratio code (if ratio-based)", example = "CURRENT_RATIO")
    private String relatedRatioCode;

    @Schema(description = "Actual calculated financial value", example = "0.8500")
    private BigDecimal actualValue;

    @Schema(description = "Triggering risk threshold value", example = "1.0000")
    private BigDecimal thresholdValue;

    @Schema(description = "Potential business and financial consequences", example = "Có nguy cơ thiếu hụt dòng tiền thanh toán các nghĩa vụ ngắn hạn.")
    private String potentialImpact;

    @Schema(description = "Deterministic actionable mitigation recommendation",
            example = "Cần tăng cường quản trị vốn lưu động, tái cấu trúc nợ ngắn hạn và tối ưu hóa vòng quay tiền mặt.")
    private String recommendation;

    @Schema(description = "Data source reference", example = "CALCULATION_RESULT_CURRENT_RATIO")
    private String sourceReference;

    @Schema(description = "Timestamp when the risk was detected")
    private LocalDateTime detectedAt;
}

