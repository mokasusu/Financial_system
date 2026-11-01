package com.financial.modules.financialindicator.controller;

import com.financial.common.config.OpenApiConfig;
import com.financial.common.response.ApiResponse;
import com.financial.common.response.PageResponse;
import com.financial.modules.financialindicator.dto.FinancialRatioInputResponse;
import com.financial.modules.financialindicator.dto.FinancialRatioResponse;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.service.FinancialIndicatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financial-ratios")
@RequiredArgsConstructor
@Tag(name = "Financial Ratio Management", description = "Endpoints for managing and querying financial ratio definitions and input dependencies")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class FinancialRatioController {

    private final FinancialIndicatorService indicatorService;

    @GetMapping
    @Operation(
            summary = "Get all financial ratio definitions",
            description = "Retrieves a paginated list of financial ratios with optional filters for category and active status"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved ratios"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required")
    })
    public ResponseEntity<ApiResponse<PageResponse<FinancialRatioResponse>>> getRatios(
            @Parameter(description = "Filter by ratio category (LIQUIDITY, SOLVENCY, PROFITABILITY, EFFICIENCY)")
            @RequestParam(required = false) FinancialRatioCategory category,
            @Parameter(description = "Filter by active status (default: true)")
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly,
            @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        var page = indicatorService.getRatios(category, activeOnly, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/core-kpis")
    @Operation(
            summary = "Get core financial health KPIs",
            description = "Retrieves the four fundamental corporate health KPIs: Current Ratio, Debt to Equity, ROA, and ROE"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved core KPIs"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required")
    })
    public ResponseEntity<ApiResponse<List<FinancialRatioResponse>>> getCoreKpis() {
        var kpis = indicatorService.getCoreKpis();
        return ResponseEntity.ok(ApiResponse.success(kpis));
    }

    @GetMapping("/category/{category}")
    @Operation(
            summary = "Get ratios by category",
            description = "Retrieves all active financial ratios within a specific category (LIQUIDITY, SOLVENCY, PROFITABILITY, EFFICIENCY)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved category ratios"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required")
    })
    public ResponseEntity<ApiResponse<List<FinancialRatioResponse>>> getRatiosByCategory(
            @Parameter(description = "Financial ratio category", required = true, example = "LIQUIDITY")
            @PathVariable FinancialRatioCategory category
    ) {
        var ratios = indicatorService.getRatiosByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(ratios));
    }

    @GetMapping("/{code}")
    @Operation(
            summary = "Get financial ratio definition by code",
            description = "Retrieves a specific financial ratio definition and its formula details by ratio code"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved ratio definition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ratio definition not found")
    })
    public ResponseEntity<ApiResponse<FinancialRatioResponse>> getRatioByCode(
            @Parameter(description = "Ratio code (e.g. CURRENT_RATIO, ROA, ROE)", required = true, example = "CURRENT_RATIO")
            @PathVariable String code
    ) {
        var ratio = indicatorService.getRatioByCode(code);
        return ResponseEntity.ok(ApiResponse.success(ratio));
    }

    @GetMapping("/{code}/inputs")
    @Operation(
            summary = "Get ratio formula input dependencies",
            description = "Retrieves the ordered list of input indicators required to compute the specified ratio, including period balance references"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved ratio inputs"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ratio definition not found")
    })
    public ResponseEntity<ApiResponse<List<FinancialRatioInputResponse>>> getRatioInputs(
            @Parameter(description = "Ratio code", required = true, example = "ROA")
            @PathVariable String code
    ) {
        var inputs = indicatorService.getRatioInputs(code);
        return ResponseEntity.ok(ApiResponse.success(inputs));
    }
}

