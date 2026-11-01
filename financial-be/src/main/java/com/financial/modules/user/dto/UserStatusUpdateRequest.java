package com.financial.modules.user.dto;

import com.financial.modules.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User account status update payload")
public class UserStatusUpdateRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "Target account status", example = "LOCKED", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserStatus status;
}
