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
@Schema(description = "Historical financial trends across confirmed reporting periods")
public class DashboardTrendResponse {

    @Schema(description = "Company ID", example = "1")
    private Long companyId;

    @Schema(description = "Company name", example = "Công ty ABC")
    private String companyName;

    @Schema(description = "List of reporting periods included in the trend", example = "[\"2022\", \"2023\", \"2024\"]")
    private List<String> availablePeriods;

    @Schema(description = "Trend data points for requested ratios/KPIs")
    private List<TrendDataPointDTO> trendData;
}

