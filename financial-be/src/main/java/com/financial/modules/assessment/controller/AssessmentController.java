package com.financial.modules.assessment.controller;

import com.financial.common.response.ApiResponse;
import com.financial.common.response.PageResponse;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.assessment.dto.*;
import com.financial.modules.assessment.service.AssessmentService;
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
@RequestMapping("/api/financial-statements/{statementId}/assessments")
@RequiredArgsConstructor
@Tag(name = "7. Financial Health Assessment", description = "Endpoints for evaluating company financial health, ratio criteria checks, and category aggregations")
@SecurityRequirement(name = "bearerAuth")
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PostMapping
    @Operation(
            summary = "Create financial health assessment",
            description = "Evaluates confirmed financial statement ratios against active evaluation criteria, aggregates results by 4 financial categories, and integrates data quality checks from the validation engine."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Financial assessment created successfully",
                    content = @Content(schema = @Schema(implementation = AssessmentResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Statement is not confirmed or calculation data is invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement or calculation not found")
    })
    public ResponseEntity<ApiResponse<AssessmentResponse>> createAssessment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @Valid @RequestBody(required = false) AssessmentRequest request
    ) {
        AssessmentResponse response = assessmentService.createAssessment(userPrincipal.getId(), statementId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Financial health assessment created successfully", response));
    }

    @GetMapping
    @Operation(
            summary = "Retrieve assessment history",
            description = "Retrieves paginated history of financial assessments executed for this financial statement."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assessment history retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement not found")
    })
    public ResponseEntity<ApiResponse<PageResponse<AssessmentHistoryResponse>>> getAssessmentHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @PageableDefault(size = 10, sort = "assessmentVersion") Pageable pageable
    ) {
        Page<AssessmentHistoryResponse> page = assessmentService.getAssessmentHistory(userPrincipal.getId(), statementId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Assessment history retrieved successfully", PageResponse.from(page)));
    }

    @GetMapping("/latest")
    @Operation(
            summary = "Retrieve latest assessment",
            description = "Retrieves the most recent financial health assessment for the statement, including category summaries and detail evaluations."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Latest assessment retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assessment or statement not found")
    })
    public ResponseEntity<ApiResponse<AssessmentResponse>> getLatestAssessment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId
    ) {
        AssessmentResponse response = assessmentService.getLatestAssessment(userPrincipal.getId(), statementId);
        return ResponseEntity.ok(ApiResponse.success("Latest assessment retrieved successfully", response));
    }

    @GetMapping("/{assessmentId}")
    @Operation(
            summary = "Retrieve assessment by ID",
            description = "Retrieves specific financial health assessment details."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assessment retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assessment not found")
    })
    public ResponseEntity<ApiResponse<AssessmentResponse>> getAssessmentById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @Parameter(description = "Assessment ID", example = "1")
            @PathVariable Long assessmentId
    ) {
        AssessmentResponse response = assessmentService.getAssessmentById(userPrincipal.getId(), statementId, assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Assessment retrieved successfully", response));
    }

    @GetMapping("/{assessmentId}/details")
    @Operation(
            summary = "Retrieve ratio evaluation details",
            description = "Retrieves individual ratio evaluation details for all 14 financial metrics."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ratio evaluation details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assessment not found")
    })
    public ResponseEntity<ApiResponse<List<AssessmentDetailResponse>>> getAssessmentDetails(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @Parameter(description = "Assessment ID", example = "1")
            @PathVariable Long assessmentId
    ) {
        List<AssessmentDetailResponse> details = assessmentService.getAssessmentDetails(userPrincipal.getId(), statementId, assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Assessment details retrieved successfully", details));
    }

    @GetMapping("/{assessmentId}/categories")
    @Operation(
            summary = "Retrieve category summaries",
            description = "Retrieves category-level evaluation summaries across Liquidity, Solvency, Profitability, and Efficiency."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category summaries retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assessment not found")
    })
    public ResponseEntity<ApiResponse<List<AssessmentCategoryResponse>>> getAssessmentCategories(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @Parameter(description = "Assessment ID", example = "1")
            @PathVariable Long assessmentId
    ) {
        List<AssessmentCategoryResponse> categories = assessmentService.getAssessmentCategories(userPrincipal.getId(), statementId, assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Category summaries retrieved successfully", categories));
    }

    @GetMapping("/{assessmentId}/summary")
    @Operation(
            summary = "Retrieve overall assessment summary",
            description = "Retrieves high-level summary of the assessment for dashboard or executive reports."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assessment summary retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assessment not found")
    })
    public ResponseEntity<ApiResponse<AssessmentSummaryResponse>> getAssessmentSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement ID", example = "1")
            @PathVariable Long statementId,
            @Parameter(description = "Assessment ID", example = "1")
            @PathVariable Long assessmentId
    ) {
        AssessmentSummaryResponse summary = assessmentService.getAssessmentSummary(userPrincipal.getId(), statementId, assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Assessment summary retrieved successfully", summary));
    }
}
