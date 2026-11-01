package com.financial.modules.financialstatement.dto;

import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.financialstatement.enums.MappingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standardized financial statement item indicator representation")
public class FinancialStatementItemResponse {

    @Schema(description = "Item ID", example = "101")
    private Long id;

    @Schema(description = "Statement report type", example = "BALANCE_SHEET")
    private FinancialStatementType statementType;

    @Schema(description = "Vietnamese display name for statement type", example = "Bảng cân đối kế toán")
    private String statementTypeDisplayName;

    @Schema(description = "Accounting standard applied", example = "TT200")
    private String accountingStandard;

    @Schema(description = "Standard indicator code", example = "100")
    private String code;

    @Schema(description = "Unique standard key across the 25 indicators", example = "CURRENT_ASSETS")
    private String key;

    @Schema(description = "Standard Vietnamese label", example = "Tài sản ngắn hạn")
    private String label;

    @Schema(description = "Raw label as extracted from original OCR document", example = "Tài sản ngắn hạn")
    private String rawLabel;

    @Schema(description = "Active current reporting period value (from editable draft)", example = "29403000000000")
    private BigDecimal current;

    @Schema(description = "Active previous reporting period value (from editable draft)", example = "28150000000000")
    private BigDecimal previous;

    @Schema(description = "Original immutable current period value from OCR", example = "29403000000000")
    private BigDecimal originalCurrentValue;

    @Schema(description = "Original immutable previous period value from OCR", example = "28150000000000")
    private BigDecimal originalPreviousValue;

    @Schema(description = "Unit of measurement", example = "VND")
    private String unit;

    @Schema(description = "Source page number in document", example = "1")
    private Integer sourcePage;

    @Schema(description = "OCR extraction confidence score (0.0 to 1.0)", example = "0.99")
    private BigDecimal confidence;

    @Schema(description = "Mapping classification status", example = "MATCHED")
    private MappingStatus mappingStatus;

    @Schema(description = "Display name for mapping classification", example = "Khớp chuẩn")
    private String mappingStatusDisplayName;

    @Schema(description = "Whether this indicator value has been edited by the user", example = "false")
    private Boolean edited;

    @Schema(description = "Timestamp when the indicator was last edited")
    private LocalDateTime editedAt;
}

