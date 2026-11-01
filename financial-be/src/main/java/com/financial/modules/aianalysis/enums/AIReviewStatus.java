package com.financial.modules.aianalysis.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "User review and approval status of AI financial analysis")
public enum AIReviewStatus {

    @Schema(description = "Pending review by financial analyst or user")
    PENDING("Chờ duyệt"),

    @Schema(description = "Approved by user without modifications")
    APPROVED("Đã phê duyệt"),

    @Schema(description = "Modified and approved by user")
    EDITED("Đã chỉnh sửa"),

    @Schema(description = "Rejected by user")
    REJECTED("Đã từ chối");

    private final String displayName;

    AIReviewStatus(String displayName) {
        this.displayName = displayName;
    }
}

