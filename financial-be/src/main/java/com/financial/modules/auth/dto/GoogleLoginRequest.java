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
@Schema(description = "Google OAuth2 ID Token login payload")
public class GoogleLoginRequest {

    @NotBlank(message = "Google ID token is required")
    @Schema(description = "Google ID token string received from Google Sign-In SDK", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ij...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String idToken;
}
