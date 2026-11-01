package com.financial.modules.auth.dto;

import com.financial.modules.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing JWT tokens and user details")
public class AuthResponse {

    @Schema(description = "Signed JWT Access Token (expires in 15 minutes)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Opaque Refresh Token (expires in 7 days, single-use rotated)", example = "7d91e4f2-95b8-4c6e-821e-f3b1456a08c1")
    private String refreshToken;

    @Builder.Default
    @Schema(description = "Token authentication scheme", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Access token lifespan in seconds", example = "900")
    private Long expiresIn;

    @Schema(description = "Public user profile details")
    private UserResponse user;
}
