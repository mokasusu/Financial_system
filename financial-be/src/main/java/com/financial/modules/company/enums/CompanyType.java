package com.financial.modules.company.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Vietnamese enterprise registration type")
public enum CompanyType {
    SINGLE_MEMBER_LIMITED_LIABILITY_COMPANY("Công ty TNHH một thành viên"),
    MULTI_MEMBER_LIMITED_LIABILITY_COMPANY("Công ty TNHH hai thành viên trở lên"),
    JOINT_STOCK_COMPANY("Công ty cổ phần"),
    PARTNERSHIP_COMPANY("Công ty hợp danh"),
    PRIVATE_ENTERPRISE("Doanh nghiệp tư nhân"),
    OTHER("Khác");

    private final String displayName;

    CompanyType(String displayName) {
        this.displayName = displayName;
    }
}

