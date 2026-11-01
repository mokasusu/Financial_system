package com.financial.modules.financialindicator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialindicator.enums.FinancialStatementType;
import com.financial.modules.financialindicator.enums.MappingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Accounting indicator mapping response")
public class AccountingIndicatorMappingResponse {

    @Schema(description = "Mapping identifier", example = "1")
    private Long id;

    @Schema(description = "Target indicator ID", example = "1")
    private Long indicatorId;

    @Schema(description = "Target indicator code", example = "CURRENT_ASSETS")
    private String indicatorCode;

    @Schema(description = "Target indicator name", example = "Tài sản ngắn hạn")
    private String indicatorName;

    @Schema(description = "Vietnamese accounting standard circular", example = "TT200")
    private AccountingStandard accountingStandard;

    @Schema(description = "Financial statement type", example = "BALANCE_SHEET")
    private FinancialStatementType statementType;

    @Schema(description = "Accounting report item code", example = "100")
    private String sourceCode;

    @Schema(description = "OCR item technical key", example = "CURRENT_ASSETS")
    private String sourceKey;

    @Schema(description = "Source label/title from standard template", example = "Tài sản ngắn hạn")
    private String sourceLabel;

    @Schema(description = "Mapping status", example = "ACTIVE")
    private MappingStatus mappingStatus;

    @Schema(description = "Effective start date", example = "2015-01-01")
    private LocalDate effectiveFrom;

    @Schema(description = "Effective end date", example = "null")
    private LocalDate effectiveTo;
}

