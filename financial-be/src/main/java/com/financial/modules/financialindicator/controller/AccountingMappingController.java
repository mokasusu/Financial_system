package com.financial.modules.financialindicator.controller;

import com.financial.common.config.OpenApiConfig;
import com.financial.common.response.ApiResponse;
import com.financial.common.response.PageResponse;
import com.financial.modules.financialindicator.dto.AccountingIndicatorMappingResponse;
import com.financial.modules.financialindicator.dto.FinancialIndicatorResponse;
import com.financial.modules.financialindicator.enums.AccountingStandard;
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
@RequestMapping("/api/accounting-mappings")
@RequiredArgsConstructor
@Tag(name = "Accounting Mapping Management", description = "Endpoints for managing and resolving OCR accounting statement line item mappings")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class AccountingMappingController {

    private final FinancialIndicatorService indicatorService;

    @GetMapping
    @Operation(
            summary = "Get all accounting indicator mappings",
            description = "Retrieves a paginated list of accounting mappings with optional filters for accounting standard and statement type"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved mappings"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required")
    })
    public ResponseEntity<ApiResponse<PageResponse<AccountingIndicatorMappingResponse>>> getMappings(
            @Parameter(description = "Filter by accounting standard (TT200, TT133, TT99)")
            @RequestParam(required = false) AccountingStandard standard,
            @Parameter(description = "Filter by statement type (BALANCE_SHEET, INCOME_STATEMENT, CASH_FLOW_STATEMENT)")
            @RequestParam(required = false) FinancialStatementType statementType,
            @PageableDefault(page = 0, size = 50, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        var page = indicatorService.getMappings(standard, statementType, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/standard/{standard}")
    @Operation(
            summary = "Get mappings by accounting standard",
            description = "Retrieves all indicator mappings defined for a specific accounting standard (TT200, TT133, TT99)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved standard mappings"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required")
    })
    public ResponseEntity<ApiResponse<List<AccountingIndicatorMappingResponse>>> getMappingsByStandard(
            @Parameter(description = "Accounting standard circular", required = true, example = "TT200")
            @PathVariable AccountingStandard standard
    ) {
        var mappings = indicatorService.getMappingsByStandard(standard);
        return ResponseEntity.ok(ApiResponse.success(mappings));
    }

    @GetMapping("/resolve")
    @Operation(
            summary = "Resolve OCR item to semantic financial indicator",
            description = "Resolves an extracted OCR statement line item to its corresponding semantic FinancialIndicator based on accounting standard, statement type, and either source code or source key"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully resolved to semantic indicator"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing or invalid query parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No matching indicator mapping found")
    })
    public ResponseEntity<ApiResponse<FinancialIndicatorResponse>> resolveMapping(
            @Parameter(description = "Accounting standard (e.g. TT200, TT133, TT99)", required = true, example = "TT200")
            @RequestParam AccountingStandard accountingStandard,
            @Parameter(description = "Financial statement type", required = true, example = "BALANCE_SHEET")
            @RequestParam FinancialStatementType statementType,
            @Parameter(description = "Accounting item report code (e.g. 100, 110, 60)", example = "100")
            @RequestParam(required = false) String sourceCode,
            @Parameter(description = "OCR extracted metric key (e.g. CURRENT_ASSETS)", example = "CURRENT_ASSETS")
            @RequestParam(required = false) String sourceKey
    ) {
        var indicator = indicatorService.resolveMapping(accountingStandard, statementType, sourceCode, sourceKey);
        return ResponseEntity.ok(ApiResponse.success(indicator));
    }
}

