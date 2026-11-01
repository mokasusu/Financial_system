package com.financial.modules.financialindicator.controller;

import com.financial.common.config.OpenApiConfig;
import com.financial.common.response.ApiResponse;
import com.financial.common.response.PageResponse;
import com.financial.modules.financialindicator.dto.FinancialIndicatorResponse;
import com.financial.modules.financialindicator.enums.FinancialStatementType;
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
@RequestMapping("/api/financial-indicators")
@RequiredArgsConstructor
@Tag(name = "Financial Indicator Management", description = "Endpoints for managing and querying financial indicators")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class FinancialIndicatorController {

    private final FinancialIndicatorService indicatorService;

    @GetMapping
    @Operation(
            summary = "Get all financial indicators",
            description = "Retrieves a paginated list of financial indicators with optional filters for statement type and active status"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved indicators"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required")
    })
    public ResponseEntity<ApiResponse<PageResponse<FinancialIndicatorResponse>>> getIndicators(
            @Parameter(description = "Filter by statement type (BALANCE_SHEET, INCOME_STATEMENT, CASH_FLOW_STATEMENT)")
            @RequestParam(required = false) FinancialStatementType statementType,
            @Parameter(description = "Filter by active status (default: true)")
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly,
            @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        var page = indicatorService.getIndicators(statementType, activeOnly, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get financial indicator by ID",
            description = "Retrieves a specific financial indicator definition by its primary key ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved indicator"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Indicator not found")
    })
    public ResponseEntity<ApiResponse<FinancialIndicatorResponse>> getIndicatorById(
            @Parameter(description = "Indicator ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        var indicator = indicatorService.getIndicatorById(id);
        return ResponseEntity.ok(ApiResponse.success(indicator));
    }

    @GetMapping("/code/{code}")
    @Operation(
            summary = "Get financial indicator by code",
            description = "Retrieves a specific financial indicator definition by its semantic indicator code"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved indicator"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Indicator not found")
    })
    public ResponseEntity<ApiResponse<FinancialIndicatorResponse>> getIndicatorByCode(
            @Parameter(description = "Semantic indicator code", required = true, example = "CURRENT_ASSETS")
            @PathVariable String code
    ) {
        var indicator = indicatorService.getIndicatorByCode(code);
        return ResponseEntity.ok(ApiResponse.success(indicator));
    }

    @GetMapping("/by-standard/{standard}")
    @Operation(
            summary = "Get financial indicators by accounting standard",
            description = "Retrieves all semantic indicators that have active mappings under the specified accounting standard (e.g. TT200, TT133, TT99)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved indicators"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Unsupported accounting standard"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required")
    })
    public ResponseEntity<ApiResponse<List<FinancialIndicatorResponse>>> getIndicatorsByStandard(
            @Parameter(description = "Accounting standard circular code (TT200, TT133, TT99)", required = true, example = "TT200")
            @PathVariable String standard
    ) {
        var indicators = indicatorService.getIndicatorsByStandard(standard);
        return ResponseEntity.ok(ApiResponse.success(indicators));
    }
}

