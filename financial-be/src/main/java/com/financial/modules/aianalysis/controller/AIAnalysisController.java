package com.financial.modules.aianalysis.controller;

import com.financial.common.response.ApiResponse;
import com.financial.common.response.PageResponse;
import com.financial.modules.aianalysis.dto.*;
import com.financial.modules.aianalysis.service.AIAnalysisService;
import com.financial.modules.auth.security.UserPrincipal;
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

@Slf4j
@RestController
@RequestMapping("/api/ai-analyses")
@RequiredArgsConstructor
@Tag(name = "AI Analysis", description = "Endpoints for generating, retrieving, reviewing, and approving AI-powered financial health analyses via OpenRouter")
@SecurityRequirement(name = "bearerAuth")
public class AIAnalysisController {

    private final AIAnalysisService aiAnalysisService;

    @PostMapping("/assessments/{assessmentId}/generate")
    @Operation(
            summary = "Generate AI financial analysis",
            description = "Triggers asynchronous AI-driven financial health analysis for an assessment via OpenRouter. Returns 202 Accepted with initial status."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "AI analysis generation initiated",
                    content = @Content(schema = @Schema(implementation = AIAnalysisResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Upstream data missing or statement not confirmed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assessment not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "An AI analysis is already currently in progress for this assessment")
    })
    public ResponseEntity<ApiResponse<AIAnalysisResponse>> generateAIAnalysis(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Assessment ID", example = "1")
            @PathVariable Long assessmentId,
            @Valid @RequestBody(required = false) AIAnalysisGenerateRequest request
    ) {
        AIAnalysisResponse response = aiAnalysisService.generateAIAnalysis(userPrincipal.getId(), assessmentId, request);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("AI analysis generation initiated", response));
    }

    @PostMapping("/statements/{statementId}/generate")
    @Operation(
            summary = "Generate or regenerate AI financial analysis by statement ID",
            description = "Triggers or regenerates AI-driven financial commentary directly using statement ID. Returns 202 Accepted."
    )
    public ResponseEntity<ApiResponse<AIAnalysisResponse>> generateAIAnalysisByStatement(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial Statement ID", example = "1")
            @PathVariable Long statementId,
            @Valid @RequestBody(required = false) AIAnalysisGenerateRequest request
    ) {
        AIAnalysisResponse response = aiAnalysisService.generateAIAnalysisForStatement(userPrincipal.getId(), statementId, request);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Tạo nhận định AI thành công", response));
    }

    @GetMapping("/assessments/{assessmentId}/latest")
    @Operation(
            summary = "Get latest AI analysis for assessment",
            description = "Retrieves the most recent AI financial health analysis for the specified assessment."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Latest AI analysis retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assessment or AI analysis not found")
    })
    public ResponseEntity<ApiResponse<AIAnalysisResponse>> getLatestAIAnalysis(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Assessment ID", example = "1")
            @PathVariable Long assessmentId
    ) {
        AIAnalysisResponse response = aiAnalysisService.getLatestAIAnalysis(userPrincipal.getId(), assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Latest AI analysis retrieved successfully", response));
    }

    @GetMapping("/{analysisId}")
    @Operation(
            summary = "Get AI analysis details by ID",
            description = "Retrieves detailed AI financial analysis including strengths, risks, recommendations, and limitations."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AI analysis details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "AI analysis not found")
    })
    public ResponseEntity<ApiResponse<AIAnalysisResponse>> getAIAnalysisById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "AI Analysis ID", example = "1")
            @PathVariable Long analysisId
    ) {
        AIAnalysisResponse response = aiAnalysisService.getAIAnalysisById(userPrincipal.getId(), analysisId);
        return ResponseEntity.ok(ApiResponse.success("AI analysis retrieved successfully", response));
    }

    @GetMapping("/assessments/{assessmentId}/history")
    @Operation(
            summary = "Get AI analysis history for assessment",
            description = "Retrieves paginated history of AI analyses generated for the specified assessment."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AI analysis history retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assessment not found")
    })
    public ResponseEntity<ApiResponse<PageResponse<AIAnalysisHistoryResponse>>> getAIAnalysisHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Assessment ID", example = "1")
            @PathVariable Long assessmentId,
            @PageableDefault(size = 10, sort = "analysisVersion") Pageable pageable
    ) {
        Page<AIAnalysisHistoryResponse> page = aiAnalysisService.getAIAnalysisHistory(userPrincipal.getId(), assessmentId, pageable);
        return ResponseEntity.ok(ApiResponse.success("AI analysis history retrieved successfully", PageResponse.from(page)));
    }

    @PutMapping("/{analysisId}/final-comment")
    @Operation(
            summary = "Update user-edited commentary",
            description = "Updates user-edited commentary for completed analysis without modifying original AI-generated comment. Sets review status to EDITED."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Final comment updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or analysis is not COMPLETED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "AI analysis not found")
    })
    public ResponseEntity<ApiResponse<AIAnalysisResponse>> updateFinalComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "AI Analysis ID", example = "1")
            @PathVariable Long analysisId,
            @Valid @RequestBody AIAnalysisFinalCommentRequest request
    ) {
        AIAnalysisResponse response = aiAnalysisService.updateFinalComment(userPrincipal.getId(), analysisId, request);
        return ResponseEntity.ok(ApiResponse.success("Final comment updated successfully", response));
    }

    @PutMapping("/statements/{statementId}/final-comment")
    @Operation(
            summary = "Update user-edited commentary directly by statement ID",
            description = "Updates user-edited commentary for the statement's latest completed AI analysis."
    )
    public ResponseEntity<ApiResponse<AIAnalysisResponse>> updateFinalCommentByStatement(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial Statement ID", example = "1")
            @PathVariable Long statementId,
            @Valid @RequestBody AIAnalysisFinalCommentRequest request
    ) {
        AIAnalysisResponse response = aiAnalysisService.updateFinalCommentForStatement(userPrincipal.getId(), statementId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhận định thành công", response));
    }

    @PostMapping("/{analysisId}/approve")
    @Operation(
            summary = "Approve AI financial analysis",
            description = "Marks the completed AI financial analysis as approved by the user."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AI analysis approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Analysis is not COMPLETED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "AI analysis not found")
    })
    public ResponseEntity<ApiResponse<AIAnalysisResponse>> approveAIAnalysis(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "AI Analysis ID", example = "1")
            @PathVariable Long analysisId
    ) {
        AIAnalysisResponse response = aiAnalysisService.approveAIAnalysis(userPrincipal.getId(), analysisId);
        return ResponseEntity.ok(ApiResponse.success("AI analysis approved successfully", response));
    }

    @PostMapping("/{analysisId}/reject")
    @Operation(
            summary = "Reject AI financial analysis",
            description = "Marks the completed AI financial analysis as rejected by the user."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AI analysis rejected successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Analysis is not COMPLETED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "AI analysis not found")
    })
    public ResponseEntity<ApiResponse<AIAnalysisResponse>> rejectAIAnalysis(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "AI Analysis ID", example = "1")
            @PathVariable Long analysisId
    ) {
        AIAnalysisResponse response = aiAnalysisService.rejectAIAnalysis(userPrincipal.getId(), analysisId);
        return ResponseEntity.ok(ApiResponse.success("AI analysis rejected successfully", response));
    }
}

