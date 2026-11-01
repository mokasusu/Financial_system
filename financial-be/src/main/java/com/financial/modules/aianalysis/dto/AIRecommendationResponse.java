package com.financial.modules.aianalysis.dto;

import com.financial.modules.aianalysis.enums.RecommendationPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Financial improvement recommendation item")
public class AIRecommendationResponse {

    @Schema(description = "Recommendation ID", example = "1")
    private Long id;

    @Schema(description = "Recommendation title", example = "Tối ưu hóa thời gian thu hồi nợ khách hàng")
    private String title;

    @Schema(description = "Detailed actionable recommendation for management")
    private String description;

    @Schema(description = "Action priority", example = "HIGH")
    private RecommendationPriority priority;

    @Schema(description = "Localized priority display name", example = "Cao")
    private String priorityDisplayName;

    @Schema(description = "Related financial risk or ratio group", example = "Vòng quay khoản phải thu")
    private String relatedRisk;

    @Schema(description = "Related risk ID if referencing an upstream identified risk", example = "101")
    private String relatedRiskId;
}

