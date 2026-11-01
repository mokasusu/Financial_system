package com.financial.modules.financialindicator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.financial.modules.financialindicator.enums.FinancialStatementType;
import com.financial.modules.financialindicator.enums.FinancialUnit;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Financial indicator definition response")
public class FinancialIndicatorResponse {

    @Schema(description = "Unique indicator identifier", example = "1")
    private Long id;

    @Schema(description = "Unique semantic code of the indicator", example = "CURRENT_ASSETS")
    private String indicatorCode;

    @Schema(description = "Vietnamese indicator name", example = "Tài sản ngắn hạn")
    private String indicatorName;

    @Schema(description = "English indicator name", example = "Current Assets")
    private String indicatorNameEn;

    @Schema(description = "Financial statement type", example = "BALANCE_SHEET")
    private FinancialStatementType statementType;

    @Schema(description = "Data type of the value", example = "DECIMAL")
    private String dataType;

    @Schema(description = "Measurement unit", example = "VND")
    private FinancialUnit unit;

    @Schema(description = "Detailed indicator description", example = "Tổng giá trị tài sản ngắn hạn của doanh nghiệp")
    private String description;

    @Schema(description = "Whether the indicator is actively used", example = "true")
    private Boolean isActive;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

