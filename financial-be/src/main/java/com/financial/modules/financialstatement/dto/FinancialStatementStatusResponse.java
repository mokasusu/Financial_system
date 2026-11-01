package com.financial.modules.financialstatement.dto;

import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Financial statement processing lifecycle status response")
public class FinancialStatementStatusResponse {

    @Schema(description = "Financial statement ID", example = "1")
    private Long id;

    @Schema(description = "Processing lifecycle status", example = "PROCESSING")
    private FinancialStatementStatus status;

    @Schema(description = "Vietnamese display name of the status", example = "Đang xử lý OCR")
    private String statusDisplayName;

    @Schema(description = "Timestamp when the file was uploaded")
    private LocalDateTime uploadedAt;

    @Schema(description = "Timestamp when background OCR processing began")
    private LocalDateTime processingStartedAt;

    @Schema(description = "Timestamp when background OCR processing completed")
    private LocalDateTime processingCompletedAt;

    @Schema(description = "Error code if processing failed", example = "OCR_TIMEOUT")
    private String errorCode;

    @Schema(description = "Error message if processing failed")
    private String errorMessage;
}

