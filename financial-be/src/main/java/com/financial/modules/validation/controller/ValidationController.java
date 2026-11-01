package com.financial.modules.validation.controller;

import com.financial.common.response.ApiResponse;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.validation.dto.*;
import com.financial.modules.validation.service.ValidationService;
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
@RequestMapping("/api/financial-statements/{statementId}/validations")
@RequiredArgsConstructor
@Tag(name = "6. Validation Engine Management", description = "Endpoints for executing accounting consistency validations and inspecting detailed validation results")
@SecurityRequirement(name = "bearerAuth")
public class ValidationController {

    private final ValidationService validationService;

    @PostMapping
    @Operation(
            summary = "Execute validation against confirmed financial statement data",
            description = "Validates accounting equation consistency, subtotals, OCR quality, and completeness for the latest confirmed data version. Rejects unconfirmed statements."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Validation executed successfully",
                    content = @Content(schema = @Schema(implementation = ValidationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Statement data is not confirmed or has invalid data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement not found")
    })
    public ResponseEntity<ApiResponse<ValidationResponse>> validate(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @Valid @RequestBody(required = false) ValidationRequest request
    ) {
        ValidationResponse response = validationService.validateStatement(userPrincipal.getId(), statementId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Financial statement validation executed successfully", response));
    }

    @GetMapping
    @Operation(
            summary = "Retrieve validation history",
            description = "Retrieves the versioned validation history summaries for the specified financial statement."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validation history retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement not found")
    })
    public ResponseEntity<ApiResponse<List<ValidationHistoryResponse>>> getValidationHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId
    ) {
        List<ValidationHistoryResponse> history = validationService.getValidationHistory(userPrincipal.getId(), statementId);
        return ResponseEntity.ok(ApiResponse.success("Validation history retrieved successfully", history));
    }

    @GetMapping("/latest")
    @Operation(
            summary = "Retrieve latest validation execution",
            description = "Retrieves the most recent validation execution and its complete rule result checklist for the specified financial statement."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Latest validation retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Validation or financial statement not found")
    })
    public ResponseEntity<ApiResponse<ValidationResponse>> getLatestValidation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId
    ) {
        ValidationResponse response = validationService.getLatestValidation(userPrincipal.getId(), statementId);
        return ResponseEntity.ok(ApiResponse.success("Latest validation retrieved successfully", response));
    }

    @GetMapping("/{executionId}")
    @Operation(
            summary = "Retrieve specific validation execution by ID",
            description = "Retrieves a historical validation execution record with its overall status and summary metrics."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validation execution retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Validation execution not found")
    })
    public ResponseEntity<ApiResponse<ValidationResponse>> getValidationById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @Parameter(description = "Validation execution ID", example = "1")
            @PathVariable Long executionId
    ) {
        ValidationResponse response = validationService.getValidationById(userPrincipal.getId(), statementId, executionId);
        return ResponseEntity.ok(ApiResponse.success("Validation execution retrieved successfully", response));
    }

    @GetMapping("/{executionId}/results")
    @Operation(
            summary = "Retrieve detailed results of a validation execution",
            description = "Retrieves the list of detailed rule results (PASS, WARNING, FAIL, REVIEW_REQUIRED) for a specific validation execution."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validation results retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Validation execution not found")
    })
    public ResponseEntity<ApiResponse<List<ValidationResultResponse>>> getValidationResults(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @Parameter(description = "Validation execution ID", example = "1")
            @PathVariable Long executionId
    ) {
        List<ValidationResultResponse> results = validationService.getValidationResults(userPrincipal.getId(), statementId, executionId);
        return ResponseEntity.ok(ApiResponse.success("Validation results retrieved successfully", results));
    }
}

