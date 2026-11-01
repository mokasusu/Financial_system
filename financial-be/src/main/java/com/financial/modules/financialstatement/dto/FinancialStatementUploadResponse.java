package com.financial.modules.financialstatement.dto;

import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Upload confirmation response returned immediately with HTTP 202")
public class FinancialStatementUploadResponse {

    @Schema(description = "Financial statement ID", example = "1")
    private Long id;

    @Schema(description = "Initial processing status", example = "QUEUED")
    private FinancialStatementStatus status;

    @Schema(description = "Status description message", example = "Financial statement has been received and queued for processing.")
    private String message;
}

