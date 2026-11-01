package com.financial.modules.user.dto;

import com.financial.modules.user.entity.User;
import com.financial.modules.user.enums.AuthProvider;
import com.financial.modules.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile response (password is never exposed)")
public class UserResponse {

    @Schema(description = "Unique user ID", example = "1")
    private Long id;

    @Schema(description = "Full name of the user", example = "Jane Doe")
    private String fullName;

    @Schema(description = "User email address", example = "jane.doe@example.com")
    private String email;

    @Schema(description = "Phone number", example = "+84912345678")
    private String phoneNumber;

    @Schema(description = "Avatar image URL", example = "https://storage.example.com/avatars/jane.png")
    private String avatarUrl;

    @Schema(description = "Account status", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Authentication provider", example = "LOCAL")
    private AuthProvider provider;

    @Schema(description = "Record creation timestamp", example = "2026-10-31T23:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last profile update timestamp", example = "2026-10-31T23:00:00")
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
