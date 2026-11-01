package com.financial.modules.evaluationcriteria.controller;

import com.financial.common.config.OpenApiConfig;
import com.financial.common.response.ApiResponse;
import com.financial.common.response.PageResponse;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaRequest;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaResponse;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaStatusRequest;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaUpdateRequest;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.evaluationcriteria.service.EvaluationCriteriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluation-criteria")
@RequiredArgsConstructor
@Tag(name = "Evaluation Criteria Management", description = "Endpoints for configuring and querying financial ratio evaluation criteria and thresholds")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class EvaluationCriteriaController {

    private final EvaluationCriteriaService criteriaService;

    @GetMapping
    @Operation(
            summary = "Get all evaluation criteria",
            description = "Retrieves a paginated list of evaluation criteria with optional filters for ratio code, active status, and comparison operator"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved evaluation criteria"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required")
    })
    public ResponseEntity<ApiResponse<PageResponse<EvaluationCriteriaResponse>>> getCriteria(
            @Parameter(description = "Filter by target ratio code (e.g. CURRENT_RATIO, ROA, ROE)", example = "CURRENT_RATIO")
            @RequestParam(required = false) String ratioCode,
            @Parameter(description = "Filter by active status", example = "true")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Filter by comparison operator", example = "GREATER_THAN_OR_EQUAL")
            @RequestParam(required = false) ComparisonOperator operator,
            @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        var page = criteriaService.getCriteria(ratioCode, isActive, operator, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get evaluation criterion by ID",
            description = "Retrieves a specific evaluation criterion definition by its primary key ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved criterion"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Evaluation criterion not found")
    })
    public ResponseEntity<ApiResponse<EvaluationCriteriaResponse>> getCriteriaById(
            @Parameter(description = "Criterion primary key ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        var result = criteriaService.getCriteriaById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/ratio/{ratioCode}")
    @Operation(
            summary = "Get criteria by ratio code",
            description = "Retrieves all evaluation criteria configured for a specific financial ratio code"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved ratio criteria"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ratio definition not found")
    })
    public ResponseEntity<ApiResponse<List<EvaluationCriteriaResponse>>> getCriteriaByRatioCode(
            @Parameter(description = "Target financial ratio semantic code", required = true, example = "CURRENT_RATIO")
            @PathVariable String ratioCode
    ) {
        var result = criteriaService.getCriteriaByRatioCode(ratioCode);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @Operation(
            summary = "Create evaluation criterion",
            description = "Creates a new evaluation criterion with threshold values. Validates that referenced ratio exists and threshold values are valid."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Criterion created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or threshold values"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Referenced ratio definition not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Active criterion already exists for this ratio")
    })
    public ResponseEntity<ApiResponse<EvaluationCriteriaResponse>> createCriteria(
            @Valid @RequestBody EvaluationCriteriaRequest request
    ) {
        var result = criteriaService.createCriteria(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Evaluation criterion created successfully", result));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update evaluation criterion",
            description = "Updates an existing evaluation criterion's thresholds, operators, and metadata"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Criterion updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or threshold values"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Evaluation criterion not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Another active criterion already exists for this ratio")
    })
    public ResponseEntity<ApiResponse<EvaluationCriteriaResponse>> updateCriteria(
            @Parameter(description = "Criterion ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody EvaluationCriteriaUpdateRequest request
    ) {
        var result = criteriaService.updateCriteria(id, request);
        return ResponseEntity.ok(ApiResponse.success("Evaluation criterion updated successfully", result));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Activate or deactivate evaluation criterion",
            description = "Updates the active status of an evaluation criterion"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing or invalid active status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Evaluation criterion not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Another active criterion already exists for this ratio")
    })
    public ResponseEntity<ApiResponse<EvaluationCriteriaResponse>> updateCriteriaStatus(
            @Parameter(description = "Criterion ID", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody(required = false) EvaluationCriteriaStatusRequest request,
            @Parameter(description = "Active status if not passed in body", example = "true")
            @RequestParam(required = false) Boolean isActive
    ) {
        Boolean active = request != null && request.getIsActive() != null ? request.getIsActive() : isActive;
        if (active == null) {
            throw new IllegalArgumentException("Active status is required in request body or as a query parameter");
        }
        var result = criteriaService.updateCriteriaStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Evaluation criterion status updated successfully", result));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete or deactivate evaluation criterion",
            description = "Deactivates an evaluation criterion to maintain assessment history and data integrity"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Criterion deactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Evaluation criterion not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCriteria(
            @Parameter(description = "Criterion ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        criteriaService.deleteCriteria(id);
        return ResponseEntity.ok(ApiResponse.success("Evaluation criterion deactivated successfully", null));
    }
}

