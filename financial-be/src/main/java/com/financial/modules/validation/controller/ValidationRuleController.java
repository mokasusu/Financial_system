package com.financial.modules.validation.controller;

import com.financial.common.response.ApiResponse;
import com.financial.common.response.PageResponse;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.validation.dto.ValidationRuleRequest;
import com.financial.modules.validation.dto.ValidationRuleResponse;
import com.financial.modules.validation.dto.ValidationRuleStatusRequest;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationSeverity;
import com.financial.modules.validation.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/validation-rules")
@RequiredArgsConstructor
@Tag(name = "Validation Rules Management", description = "Endpoints for configuring and querying financial validation rules, tolerances, and active statuses")
@SecurityRequirement(name = "bearerAuth")
public class ValidationRuleController {

    private final ValidationService validationService;

    @GetMapping
    @Operation(
            summary = "List validation rules with filters",
            description = "Retrieves paginated validation rules with optional filters by standard, statement type, category, severity, and active status."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validation rules retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<PageResponse<ValidationRuleResponse>>> getRules(
            @Parameter(description = "Filter by accounting standard (TT200, TT133, TT99)")
            @RequestParam(required = false) AccountingStandard accountingStandard,
            @Parameter(description = "Filter by statement type (BALANCE_SHEET, INCOME_STATEMENT, CASH_FLOW)")
            @RequestParam(required = false) FinancialStatementType statementType,
            @Parameter(description = "Filter by validation category")
            @RequestParam(required = false) ValidationCategory category,
            @Parameter(description = "Filter by severity level")
            @RequestParam(required = false) ValidationSeverity severity,
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20, sort = "ruleCode") Pageable pageable
    ) {
        Page<ValidationRuleResponse> page = validationService.getValidationRules(
                accountingStandard, statementType, category, severity, isActive, pageable
        );
        return ResponseEntity.ok(ApiResponse.success("Validation rules retrieved successfully", PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Retrieve validation rule by ID",
            description = "Retrieves configuration details of a specific validation rule."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validation rule retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Validation rule not found")
    })
    public ResponseEntity<ApiResponse<ValidationRuleResponse>> getRuleById(
            @Parameter(description = "Rule ID", example = "1")
            @PathVariable Long id
    ) {
        ValidationRuleResponse response = validationService.getValidationRuleById(id);
        return ResponseEntity.ok(ApiResponse.success("Validation rule retrieved successfully", response));
    }

    @PostMapping
    @Operation(
            summary = "Create a validation rule",
            description = "Creates a new validation rule configuration with unique rule code, category, severity, and tolerance."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Validation rule created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or duplicate rule code"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<ValidationRuleResponse>> createRule(
            @Valid @RequestBody ValidationRuleRequest request
    ) {
        ValidationRuleResponse response = validationService.createValidationRule(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Validation rule created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing validation rule",
            description = "Updates the configuration, severity, tolerance, or description of an existing validation rule."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validation rule updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Validation rule not found")
    })
    public ResponseEntity<ApiResponse<ValidationRuleResponse>> updateRule(
            @Parameter(description = "Rule ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ValidationRuleRequest request
    ) {
        ValidationRuleResponse response = validationService.updateValidationRule(id, request);
        return ResponseEntity.ok(ApiResponse.success("Validation rule updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Activate or deactivate a validation rule",
            description = "Toggles the active state of a validation rule."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validation rule status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Validation rule not found")
    })
    public ResponseEntity<ApiResponse<ValidationRuleResponse>> updateRuleStatus(
            @Parameter(description = "Rule ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ValidationRuleStatusRequest request
    ) {
        ValidationRuleResponse response = validationService.updateValidationRuleStatus(id, request.getIsActive());
        return ResponseEntity.ok(ApiResponse.success("Validation rule status updated successfully", response));
    }
}

