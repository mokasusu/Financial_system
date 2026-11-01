package com.financial.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI financial recommendation item")
public class DashboardAIRecommendationDTO {

    @Schema(description = "Recommendation title", example = "Tối ưu hóa kỳ thu hồi công nợ")
    private String title;

    @Schema(description = "Actionable detailed recommendation")
    private String description;

    @Schema(description = "Priority level", example = "HIGH")
    private String priority;

    @Schema(description = "Related risk ID if applicable", example = "101")
    private String relatedRiskId;
}

