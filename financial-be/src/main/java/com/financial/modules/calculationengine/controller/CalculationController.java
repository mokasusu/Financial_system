package com.financial.modules.calculationengine.controller;

import com.financial.common.response.ApiResponse;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.calculationengine.dto.CalculationHistoryResponse;
import com.financial.modules.calculationengine.dto.CalculationRequest;
import com.financial.modules.calculationengine.dto.CalculationResponse;
import com.financial.modules.calculationengine.dto.CalculationResultResponse;
import com.financial.modules.calculationengine.service.CalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/financial-statements/{statementId}/calculations")
@RequiredArgsConstructor
@Tag(name = "5. Calculation Engine Management", description = "Endpoints for executing financial ratio and core KPI calculations, tracking calculation history, and viewing calculation details")
@SecurityRequirement(name = "bearerAuth")
public class CalculationController {

    private final CalculationService calculationService;

    @PostMapping
    @Operation(
            summary = "Trigger official financial ratio calculation",
            description = "Calculates all 14 official financial ratios and 4 core KPIs using the statement's latest confirmed data version. Rejects unconfirmed statements."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Financial ratios calculated successfully",
                    content = @Content(schema = @Schema(implementation = CalculationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Statement data is not confirmed or has invalid data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement not found")
    })
    public ResponseEntity<ApiResponse<CalculationResponse>> calculate(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @Valid @RequestBody(required = false) CalculationRequest request
    ) {
        CalculationResponse response = calculationService.calculateForStatement(userPrincipal.getId(), statementId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Financial ratios calculated successfully", response));
    }

    @GetMapping
    @Operation(
            summary = "Retrieve calculation history",
            description = "Retrieves the versioned calculation history for the specified financial statement."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Calculation history retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement not found")
    })
    public ResponseEntity<ApiResponse<List<CalculationHistoryResponse>>> getCalculationHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId
    ) {
        List<CalculationHistoryResponse> history = calculationService.getCalculationHistory(userPrincipal.getId(), statementId);
        return ResponseEntity.ok(ApiResponse.success("Calculation history retrieved successfully", history));
    }

    @GetMapping("/latest")
    @Operation(
            summary = "Retrieve latest calculation",
            description = "Retrieves the latest calculation execution with all 14 ratio results for the specified financial statement."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Latest calculation retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement or calculation not found")
    })
    public ResponseEntity<ApiResponse<CalculationResponse>> getLatestCalculation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId
    ) {
        CalculationResponse response = calculationService.getLatestCalculation(userPrincipal.getId(), statementId);
        return ResponseEntity.ok(ApiResponse.success("Latest calculation retrieved successfully", response));
    }

    @GetMapping("/{calculationId}")
    @Operation(
            summary = "Retrieve specific calculation by ID",
            description = "Retrieves a historical calculation by its ID with all associated ratio results."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Calculation retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Calculation or statement not found")
    })
    public ResponseEntity<ApiResponse<CalculationResponse>> getCalculationById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @Parameter(description = "Calculation ID", example = "1")
            @PathVariable Long calculationId
    ) {
        CalculationResponse response = calculationService.getCalculationById(userPrincipal.getId(), statementId, calculationId);
        return ResponseEntity.ok(ApiResponse.success("Calculation retrieved successfully", response));
    }

    @GetMapping("/latest/kpis")
    @Operation(
            summary = "Retrieve 4 core KPIs from latest calculation",
            description = "Retrieves the four core KPIs (CURRENT_RATIO, DEBT_TO_EQUITY, ROA, ROE) from the latest calculation."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Core KPIs retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Calculation or statement not found")
    })
    public ResponseEntity<ApiResponse<List<CalculationResultResponse>>> getLatestCoreKpis(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId
    ) {
        List<CalculationResultResponse> kpis = calculationService.getLatestCoreKpis(userPrincipal.getId(), statementId);
        return ResponseEntity.ok(ApiResponse.success("Core KPIs retrieved successfully", kpis));
    }
}

