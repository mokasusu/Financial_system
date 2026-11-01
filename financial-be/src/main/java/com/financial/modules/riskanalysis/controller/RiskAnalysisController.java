package com.financial.modules.riskanalysis.controller;

import com.financial.common.response.ApiResponse;
import com.financial.common.response.PageResponse;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.riskanalysis.dto.*;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import com.financial.modules.riskanalysis.service.RiskAnalysisService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/financial-statements/{statementId}/risk-analyses")
@RequiredArgsConstructor
@Tag(name = "8. Financial Risk Analysis", description = "Endpoints for detecting, aggregating, and tracking financial risks based on calculation and assessment outcomes")
@SecurityRequirement(name = "bearerAuth")
public class RiskAnalysisController {

    private final RiskAnalysisService riskAnalysisService;

    @PostMapping
    @Operation(
            summary = "Execute financial risk analysis",
            description = "Analyzes financial risks across Liquidity, Solvency, Profitability, Efficiency, and Data Quality based on configured rules and confirmed statement assessments."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Risk analysis executed successfully",
                    content = @Content(schema = @Schema(implementation = RiskAnalysisResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Statement is not confirmed or has mismatched versions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Statement, assessment, or calculation not found")
    })
    public ResponseEntity<ApiResponse<RiskAnalysisResponse>> analyzeRisks(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @Valid @RequestBody(required = false) RiskAnalysisRequest request
    ) {
        RiskAnalysisResponse response = riskAnalysisService.analyzeRisks(userPrincipal.getId(), statementId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Financial risk analysis executed successfully", response));
    }

    @GetMapping
    @Operation(
            summary = "Retrieve risk analysis history",
            description = "Retrieves paginated history of risk analyses executed for this financial statement."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Risk analysis history retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Statement not found")
    })
    public ResponseEntity<ApiResponse<PageResponse<RiskAnalysisHistoryResponse>>> getRiskAnalysisHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @PageableDefault(size = 10, sort = "analysisVersion") Pageable pageable
    ) {
        Page<RiskAnalysisHistoryResponse> page = riskAnalysisService.getRiskAnalysisHistory(userPrincipal.getId(), statementId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Risk analysis history retrieved successfully", PageResponse.from(page)));
    }

    @GetMapping("/latest")
    @Operation(
            summary = "Retrieve latest risk analysis",
            description = "Retrieves the most recent financial risk analysis result for the statement."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Latest risk analysis retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Risk analysis or statement not found")
    })
    public ResponseEntity<ApiResponse<RiskAnalysisResponse>> getLatestRiskAnalysis(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId
    ) {
        RiskAnalysisResponse response = riskAnalysisService.getLatestRiskAnalysis(userPrincipal.getId(), statementId);
        return ResponseEntity.ok(ApiResponse.success("Latest risk analysis retrieved successfully", response));
    }

    @GetMapping("/{analysisId}")
    @Operation(
            summary = "Retrieve specific risk analysis by ID",
            description = "Retrieves details of a specific risk analysis."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Risk analysis retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Risk analysis not found")
    })
    public ResponseEntity<ApiResponse<RiskAnalysisResponse>> getRiskAnalysisById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @Parameter(description = "Risk analysis ID", example = "1")
            @PathVariable Long analysisId
    ) {
        RiskAnalysisResponse response = riskAnalysisService.getRiskAnalysisById(userPrincipal.getId(), statementId, analysisId);
        return ResponseEntity.ok(ApiResponse.success("Risk analysis retrieved successfully", response));
    }

    @GetMapping("/{analysisId}/risks")
    @Operation(
            summary = "Retrieve identified risks with optional filters",
            description = "Retrieves individual identified risks, with optional filtering by category, severity, and status."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Identified risks retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Risk analysis not found")
    })
    public ResponseEntity<ApiResponse<List<IdentifiedRiskResponse>>> getIdentifiedRisks(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @Parameter(description = "Risk analysis ID", example = "1")
            @PathVariable Long analysisId,
            @Parameter(description = "Filter by risk category")
            @RequestParam(required = false) RiskCategory category,
            @Parameter(description = "Filter by severity level")
            @RequestParam(required = false) RiskSeverity severity,
            @Parameter(description = "Filter by risk status (DETECTED, MONITORING, RESOLVED, NOT_CONFIGURED, INSUFFICIENT_DATA)")
            @RequestParam(required = false) RiskStatus status
    ) {
        List<IdentifiedRiskResponse> risks = riskAnalysisService.getIdentifiedRisks(
                userPrincipal.getId(), statementId, analysisId, category, severity, status
        );
        return ResponseEntity.ok(ApiResponse.success("Identified risks retrieved successfully", risks));
    }

    @GetMapping("/{analysisId}/summary")
    @Operation(
            summary = "Retrieve risk summary by category and severity",
            description = "Retrieves aggregated risk count breakdown by category and severity for dashboard cards and visualizations."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Risk summary retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Risk analysis not found")
    })
    public ResponseEntity<ApiResponse<RiskSummaryResponse>> getRiskSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @Parameter(description = "Risk analysis ID", example = "1")
            @PathVariable Long analysisId
    ) {
        RiskSummaryResponse summary = riskAnalysisService.getRiskSummary(userPrincipal.getId(), statementId, analysisId);
        return ResponseEntity.ok(ApiResponse.success("Risk summary retrieved successfully", summary));
    }
}

