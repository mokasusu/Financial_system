package com.financial.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refresh token exchange or revocation payload")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "Raw refresh token issued at login or register", example = "48b64b18-b80c-4ff6-9db6-dc929ebf40f2", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}
