package com.financial.modules.assessment.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Assessment input data quality status based on validation engine results")
public enum DataQualityStatus {
    @Schema(description = "Dữ liệu báo cáo tài chính hoàn toàn hợp lệ, không vi phạm quy tắc kế toán")
    VALID,

    @Schema(description = "Dữ liệu có cảnh báo hoặc biến động nhưng không vi phạm nghiêm trọng")
    HAS_WARNINGS,

    @Schema(description = "Dữ liệu vi phạm nghiêm trọng quy tắc hoặc phương trình kế toán")
    INVALID,

    @Schema(description = "Dữ liệu cần rà soát lại trước khi tin cậy (hoặc lệch phiên bản dữ liệu)")
    REVIEW_REQUIRED,

    @Schema(description = "Báo cáo tài chính chưa thực hiện bước kiểm tra (validation)")
    NOT_VALIDATED
}

