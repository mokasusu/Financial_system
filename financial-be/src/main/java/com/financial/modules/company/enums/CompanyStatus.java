package com.financial.modules.company.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Operating status of the company")
public enum CompanyStatus {
    ACTIVE("Đang hoạt động"),
    INACTIVE("Tạm ngừng hoạt động");

    private final String displayName;

    CompanyStatus(String displayName) {
        this.displayName = displayName;
    }
}

