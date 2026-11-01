package com.financial.modules.riskanalysis.controller;

import com.financial.common.response.ApiResponse;
import com.financial.common.response.PageResponse;
import com.financial.modules.riskanalysis.dto.RiskRuleRequest;
import com.financial.modules.riskanalysis.dto.RiskRuleResponse;
import com.financial.modules.riskanalysis.dto.RiskRuleStatusRequest;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
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
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/risk-rules")
@RequiredArgsConstructor
@Tag(name = "8.1. Risk Rule Management", description = "Endpoints for configuring and managing financial risk detection rules, thresholds, and templates")
@SecurityRequirement(name = "bearerAuth")
public class RiskRuleController {

    private final RiskAnalysisService riskAnalysisService;

    @GetMapping
    @Operation(
            summary = "List risk rules with filters",
            description = "Retrieves paginated risk rules with optional filters by risk category, ratio code, severity, and active status."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Risk rules retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<PageResponse<RiskRuleResponse>>> getRiskRules(
            @Parameter(description = "Filter by risk category")
            @RequestParam(required = false) RiskCategory category,
            @Parameter(description = "Filter by related financial ratio code")
            @RequestParam(required = false) String ratioCode,
            @Parameter(description = "Filter by risk severity")
            @RequestParam(required = false) RiskSeverity severity,
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20, sort = "ruleCode") Pageable pageable
    ) {
        Page<RiskRuleResponse> page = riskAnalysisService.getRiskRules(category, ratioCode, severity, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success("Risk rules retrieved successfully", PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Retrieve risk rule by ID",
            description = "Retrieves details of a specific financial risk rule."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Risk rule retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Risk rule not found")
    })
    public ResponseEntity<ApiResponse<RiskRuleResponse>> getRiskRuleById(
            @Parameter(description = "Risk rule ID", example = "1")
            @PathVariable Long id
    ) {
        RiskRuleResponse response = riskAnalysisService.getRiskRuleById(id);
        return ResponseEntity.ok(ApiResponse.success("Risk rule retrieved successfully", response));
    }

    @PostMapping
    @Operation(
            summary = "Create new risk rule",
            description = "Registers a new financial risk rule with trigger condition, severity, and explanation templates."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Risk rule created successfully",
                    content = @Content(schema = @Schema(implementation = RiskRuleResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid risk rule input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Rule code already exists")
    })
    public ResponseEntity<ApiResponse<RiskRuleResponse>> createRiskRule(
            @Valid @RequestBody RiskRuleRequest request
    ) {
        RiskRuleResponse response = riskAnalysisService.createRiskRule(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Risk rule created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update risk rule",
            description = "Updates configuration details, thresholds, and templates for an existing risk rule."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Risk rule updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid risk rule input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Risk rule not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Rule code already in use by another rule")
    })
    public ResponseEntity<ApiResponse<RiskRuleResponse>> updateRiskRule(
            @Parameter(description = "Risk rule ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody RiskRuleRequest request
    ) {
        RiskRuleResponse response = riskAnalysisService.updateRiskRule(id, request);
        return ResponseEntity.ok(ApiResponse.success("Risk rule updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Activate or deactivate risk rule",
            description = "Enables or disables a financial risk rule."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Risk rule status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Risk rule not found")
    })
    public ResponseEntity<ApiResponse<RiskRuleResponse>> updateRiskRuleStatus(
            @Parameter(description = "Risk rule ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody RiskRuleStatusRequest request
    ) {
        RiskRuleResponse response = riskAnalysisService.updateRiskRuleStatus(id, request.getIsActive());
        return ResponseEntity.ok(ApiResponse.success("Risk rule status updated successfully", response));
    }
}

