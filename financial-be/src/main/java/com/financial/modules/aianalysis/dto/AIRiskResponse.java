package com.financial.modules.aianalysis.dto;

import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Identified financial risk summary item")
public class AIRiskResponse {

    @Schema(description = "Risk ID", example = "1")
    private Long id;

    @Schema(description = "Risk title", example = "Đòn bẩy tài chính ở mức rủi ro cao")
    private String title;

    @Schema(description = "Detailed description of the financial risk")
    private String description;

    @Schema(description = "Authoritative risk severity", example = "HIGH")
    private RiskSeverity severity;

    @Schema(description = "Localized severity display name", example = "Cao")
    private String severityDisplayName;

    @Schema(description = "Risk category", example = "SOLVENCY")
    private RiskCategory category;

    @Schema(description = "Localized category display name", example = "Đòn bẩy & Khả năng trả nợ")
    private String categoryDisplayName;

    @Schema(description = "Supporting evidence")
    private List<String> evidence;

    @Schema(description = "Matched upstream identified risk ID if available", example = "101")
    private Long sourceRiskId;
}

