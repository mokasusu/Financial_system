package com.financial.modules.dashboard.dto;

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
@Schema(description = "AI financial analysis risk item")
public class DashboardAIRiskDTO {

    @Schema(description = "Risk title", example = "Đòn bẩy tài chính ở mức rủi ro cao")
    private String title;

    @Schema(description = "Detailed risk description")
    private String description;

    @Schema(description = "Authoritative risk severity", example = "HIGH")
    private String severity;

    @Schema(description = "Risk category", example = "SOLVENCY")
    private String category;

    @Schema(description = "Supporting evidence list")
    private List<String> evidence;

    @Schema(description = "Source upstream risk ID if linked", example = "101")
    private Long sourceRiskId;
}

