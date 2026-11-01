package com.financial.modules.riskanalysis.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Overall status of the risk analysis execution")
public enum RiskAnalysisStatus {
    @Schema(description = "Phân tích rủi ro hoàn tất thành công")
    COMPLETED,

    @Schema(description = "Phân tích rủi ro hoàn tất với một số thành phần dữ liệu khuyết thiếu")
    INCOMPLETE,

    @Schema(description = "Phân tích rủi ro thất bại do lỗi dữ liệu hoặc hệ thống")
    FAILED
}

