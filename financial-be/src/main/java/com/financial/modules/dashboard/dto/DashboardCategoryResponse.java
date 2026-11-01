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
@Schema(description = "Financial ratios grouped by analytical category")
public class DashboardCategoryResponse {

    @Schema(description = "Category code", example = "LIQUIDITY")
    private String category;

    @Schema(description = "Category localized display name", example = "Thanh khoản")
    private String displayName;

    @Schema(description = "List of financial ratios belonging to this category")
    private List<DashboardRatioResponse> ratios;
}

