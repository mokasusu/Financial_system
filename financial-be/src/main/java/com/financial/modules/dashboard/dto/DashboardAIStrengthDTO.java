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
@Schema(description = "AI financial analysis strength item")
public class DashboardAIStrengthDTO {

    @Schema(description = "Strength title", example = "Khả năng thanh toán hiện hành vững chắc")
    private String title;

    @Schema(description = "Detailed description")
    private String description;

    @Schema(description = "Supporting evidence list")
    private List<String> evidence;
}

