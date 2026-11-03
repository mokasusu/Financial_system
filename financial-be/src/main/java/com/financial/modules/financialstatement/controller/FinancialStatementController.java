package com.financial.modules.financialstatement.controller;

import com.financial.common.config.OpenApiConfig;
import com.financial.common.response.ApiResponse;
import com.financial.common.response.PageResponse;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.financialstatement.dto.*;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.service.FinancialStatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/financial-statements")
@Tag(name = "4. Financial Statement Management", description = "Endpoints for uploading financial statements, managing OCR processing lifecycle, reviewing and editing indicators")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class FinancialStatementController {

    private final FinancialStatementService financialStatementService;

    public FinancialStatementController(FinancialStatementService financialStatementService) {
        this.financialStatementService = financialStatementService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Upload financial statement file",
            description = "Accepts a financial statement file (PDF, PNG, JPG, TIFF, BMP, WEBP, TXT up to 25MB). " +
                          "Saves the record in QUEUED status and returns HTTP 202 Accepted immediately. " +
                          "A background worker calls the external OCR API (with up to 600 seconds timeout) to extract financial metrics."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Statement accepted and queued for processing"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file, empty file, unsupported extension, or file too large"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company profile not found for user")
    })
    public ResponseEntity<ApiResponse<FinancialStatementUploadResponse>> uploadFinancialStatement(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Financial statement document file (PDF, PNG, JPG, etc. up to 25MB)", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Reporting period: 'current' or 'previous'", example = "current")
            @RequestParam(value = "period", required = false, defaultValue = "current") String period,
            @Parameter(description = "Enable AI interpretation during OCR", example = "true")
            @RequestParam(value = "enableAi", required = false, defaultValue = "true") Boolean enableAi,
            @Parameter(description = "Store evidence crop images in OCR", example = "true")
            @RequestParam(value = "storeImages", required = false, defaultValue = "true") Boolean storeImages) {

        FinancialStatementUploadResponse response = financialStatementService.uploadFinancialStatement(
                userPrincipal.getId(), file, period, enableAi, storeImages
        );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Financial statement uploaded and queued for processing", response));
    }

    @PostMapping("/{id}/extract")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Trigger OCR extraction",
            description = "Triggers or retries background OCR extraction via /api/v1/extract for an uploaded financial statement."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Extraction queued successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement not found")
    })
    public ResponseEntity<ApiResponse<FinancialStatementStatusResponse>> triggerExtraction(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        FinancialStatementStatusResponse response = financialStatementService.triggerExtraction(userPrincipal.getId(), id);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("OCR extraction triggered and queued", response));
    }

    @GetMapping("/{id}/status")
    @Operation(
            summary = "Get processing lifecycle status",
            description = "Retrieves the processing status of a financial statement (QUEUED, PROCESSING, COMPLETED, FAILED, CANCEL_REQUESTED, CANCELLED, REVIEWING_DATA, DATA_CONFIRMED)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement not found")
    })
    public ResponseEntity<ApiResponse<FinancialStatementStatusResponse>> getStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        FinancialStatementStatusResponse response = financialStatementService.getFinancialStatementStatus(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get financial statement details",
            description = "Retrieves complete metadata, document information, accounting standard, processing status, and mapped items."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statement retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement not found")
    })
    public ResponseEntity<ApiResponse<FinancialStatementResponse>> getFinancialStatement(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        FinancialStatementResponse response = financialStatementService.getFinancialStatement(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/file")
    @Operation(
            summary = "View or download uploaded original financial statement document",
            description = "Streams the original uploaded file (PDF/Image) with Content-Disposition inline for browser viewing."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File streamed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Statement or file not found")
    })
    public ResponseEntity<Resource> getStatementFile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        FinancialStatementService.StatementFileDownload download = financialStatementService.getStatementFile(userPrincipal.getId(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + download.filename() + "\"")
                .body(download.resource());
    }

    @GetMapping("/{id}/items")
    @Operation(
            summary = "Get mapped 25 financial indicators",
            description = "Retrieves the standardized list of the 25 core indicators across Balance Sheet, Income Statement, and Cash Flow. " +
                          "Includes both original OCR values and user-editable draft values."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Indicators retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement not found")
    })
    public ResponseEntity<ApiResponse<List<FinancialStatementItemResponse>>> getItems(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        List<FinancialStatementItemResponse> items = financialStatementService.getFinancialStatementItems(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PutMapping("/{id}/items")
    @Operation(
            summary = "Update financial indicator draft values",
            description = "Allows editing current and previous period values for extracted indicators. " +
                          "Original OCR extraction values are preserved immutably. Once confirmed, editing is disabled."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Draft values updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Statement already confirmed or invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Statement or indicator item not found")
    })
    public ResponseEntity<ApiResponse<List<FinancialStatementItemResponse>>> updateItems(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody FinancialStatementItemUpdateRequest request) {

        List<FinancialStatementItemResponse> response = financialStatementService.updateFinancialStatementItems(
                userPrincipal.getId(), id, request
        );
        return ResponseEntity.ok(ApiResponse.success("Financial indicator values updated successfully", response));
    }

    @PostMapping("/{id}/items/reset")
    @Operation(
            summary = "Reset indicator values to original OCR values",
            description = "Restores all edited draft indicator values back to the original OCR extraction values."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Draft values reset to original OCR values"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Statement already confirmed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement not found")
    })
    public ResponseEntity<ApiResponse<List<FinancialStatementItemResponse>>> resetItems(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        List<FinancialStatementItemResponse> response = financialStatementService.resetFinancialStatementItems(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Indicators successfully reset to original OCR values", response));
    }

    @PostMapping("/{id}/confirm")
    @Operation(
            summary = "Confirm financial statement data",
            description = "Finalizes the statement data after user review and transitions status to DATA_CONFIRMED. " +
                          "Prepares the statement for subsequent financial ratio and assessment calculation workflows."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Financial statement data confirmed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Statement not in COMPLETED or REVIEWING_DATA status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement not found")
    })
    public ResponseEntity<ApiResponse<FinancialStatementResponse>> confirmData(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        FinancialStatementResponse response = financialStatementService.confirmFinancialData(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Financial statement data confirmed successfully", response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel statement processing",
            description = "Requests cancellation of statement processing. If in QUEUED status, marks CANCELLED immediately. " +
                          "If in PROCESSING status, marks CANCEL_REQUESTED so the background worker discards OCR results upon completion. " +
                          "Note: Cancelling the backend job does not guarantee the remote external OCR server halts computation."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cancellation handled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Statement already completed or failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement not found")
    })
    public ResponseEntity<ApiResponse<FinancialStatementStatusResponse>> cancelProcessing(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        FinancialStatementStatusResponse response = financialStatementService.cancelProcessing(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Cancellation requested", response));
    }

    @GetMapping
    @Operation(
            summary = "List company financial statements",
            description = "Returns a paginated list of financial statements belonging exclusively to the authenticated user's company, with optional filters."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statements retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found for user")
    })
    public ResponseEntity<ApiResponse<PageResponse<FinancialStatementResponse>>> getFinancialStatements(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Filter by fiscal year", example = "2023")
            @RequestParam(required = false) Integer fiscalYear,
            @Parameter(description = "Filter by reporting period", example = "current")
            @RequestParam(required = false) String period,
            @Parameter(description = "Filter by processing status")
            @RequestParam(required = false) FinancialStatementStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<FinancialStatementResponse> response = financialStatementService.getFinancialStatements(
                userPrincipal.getId(), fiscalYear, period, status, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/finalize")
    @Operation(
            summary = "Finalize financial statement session",
            description = "Transitions a draft financial statement session to official finalized status, integrating it into company analytics and dashboard."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statement finalized successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Statement data not confirmed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement not found")
    })
    public ResponseEntity<ApiResponse<FinancialStatementResponse>> finalizeFinancialStatement(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        FinancialStatementResponse response = financialStatementService.finalizeStatement(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Lưu và phê duyệt báo cáo tài chính thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete financial statement session",
            description = "Permanently deletes a financial statement session, its extracted indicators, draft edits, downstream calculations, validations, risk assessments, AI commentary, and uploaded physical file."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statement session deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Financial statement not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteFinancialStatement(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        financialStatementService.deleteFinancialStatement(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Xóa phiên báo cáo tài chính thành công", null));
    }
}

