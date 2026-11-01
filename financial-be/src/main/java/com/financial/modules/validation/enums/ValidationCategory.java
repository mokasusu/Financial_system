package com.financial.modules.validation.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Validation rule category")
public enum ValidationCategory {

    @Schema(description = "Balance sheet fundamental accounting equations (Total Assets = Liabilities + Equity)")
    ACCOUNTING_EQUATION("Phương trình kế toán"),

    @Schema(description = "Balance sheet component totals and subtotals consistency")
    BALANCE_SHEET_SUBTOTAL("Tổng phụ Bảng cân đối"),

    @Schema(description = "Income statement line item and profit subtotal relationships")
    INCOME_STATEMENT("Quan hệ Báo cáo kết quả kinh doanh"),

    @Schema(description = "Cross-period consistency, significant fluctuations, and negative value checks")
    CROSS_PERIOD("Kiểm tra liên kỳ"),

    @Schema(description = "OCR extraction quality, mapping status, and confidence warnings")
    OCR_DATA_QUALITY("Chất lượng dữ liệu OCR"),

    @Schema(description = "Confirmed data completeness and required indicator presence")
    CONFIRMED_DATA("Kiểm tra dữ liệu đã xác nhận");

    private final String displayName;

    ValidationCategory(String displayName) {
        this.displayName = displayName;
    }
}

