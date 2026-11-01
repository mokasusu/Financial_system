package com.financial.modules.validation.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Validation tolerance calculation type")
public enum ToleranceType {

    @Schema(description = "Absolute monetary amount difference (e.g. 1000 VND)")
    ABSOLUTE("Số tiền tuyệt đối"),

    @Schema(description = "Percentage difference relative to expected base value (e.g. 0.01%)")
    PERCENTAGE("Phần trăm");

    private final String displayName;

    ToleranceType(String displayName) {
        this.displayName = displayName;
    }
}

