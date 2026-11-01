package com.financial.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Global context identifying the active company, reporting period, and financial statement")
public class DashboardContextResponse {

    @Schema(description = "Company ID", example = "1")
    private Long companyId;

    @Schema(description = "Company name", example = "Công ty Cổ phần Công nghệ ABC")
    private String companyName;

    @Schema(description = "Tax code", example = "0101234567")
    private String taxCode;

    @Schema(description = "Main business line or industry", example = "Công nghệ thông tin")
    private String businessSector;

    @Schema(description = "Active accounting standard", example = "TT200")
    private String accountingStandard;

    @Schema(description = "Selected reporting period", example = "2024")
    private String selectedPeriod;

    @Schema(description = "Currency unit", example = "VND")
    private String currencyUnit;

    @Schema(description = "Financial statement ID", example = "10")
    private Long financialStatementId;

    @Schema(description = "Financial statement processing status", example = "DATA_CONFIRMED")
    private String financialStatementProcessingStatus;

    @Schema(description = "Whether the user has a registered active company", example = "true")
    private Boolean hasCompany;

    @Schema(description = "Whether an active financial statement was found", example = "true")
    private Boolean hasFinancialStatement;

    @Schema(description = "Overall status of the dashboard context data", example = "READY")
    private String dataStatus;
}

