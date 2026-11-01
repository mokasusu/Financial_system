package com.financial.modules.financialstatement.dto;

import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed financial statement response including metadata and mapped items")
public class FinancialStatementResponse {

    @Schema(description = "Financial statement ID", example = "1")
    private Long id;

    @Schema(description = "Associated company ID", example = "10")
    private Long companyId;

    @Schema(description = "Company registered name", example = "Công ty Cổ phần Sao Mai")
    private String companyName;

    @Schema(description = "Original uploaded file name", example = "BCTC_2023_Audited.pdf")
    private String fileName;

    @Schema(description = "File size in bytes", example = "1048576")
    private Long fileSize;

    @Schema(description = "MIME content type", example = "application/pdf")
    private String contentType;

    @Schema(description = "Accounting standard applied", example = "TT200")
    private String accountingStandard;

    @Schema(description = "Fiscal reporting year", example = "2023")
    private Integer fiscalYear;

    @Schema(description = "Reporting period", example = "current")
    private String period;

    @Schema(description = "As of reporting date", example = "2023-12-31")
    private LocalDate asOfDate;

    @Schema(description = "Currency unit of measure", example = "VND")
    private String unit;

    @Schema(description = "Processing lifecycle status", example = "COMPLETED")
    private FinancialStatementStatus processingStatus;

    @Schema(description = "Display name of processing status", example = "Xử lý OCR thành công")
    private String statusDisplayName;

    @Schema(description = "OCR tracking request ID", example = "ocr-req-12345")
    private String ocrRequestId;

    @Schema(description = "Error code if processing failed", example = "OCR_TIMEOUT")
    private String errorCode;

    @Schema(description = "Error message if processing failed")
    private String errorMessage;

    @Schema(description = "Upload timestamp")
    private LocalDateTime uploadedAt;

    @Schema(description = "Processing start timestamp")
    private LocalDateTime processingStartedAt;

    @Schema(description = "Processing completion timestamp")
    private LocalDateTime processingCompletedAt;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "List of standardized financial indicators")
    private List<FinancialStatementItemResponse> items;
}

