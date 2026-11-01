package com.financial.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile update payload")
public class UserUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(description = "Updated full name", example = "Jane Doe Updated", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(description = "Updated email address", example = "jane.updated@example.com")
    private String email;

    @Schema(description = "Updated phone number", example = "+84988776655")
    private String phoneNumber;

    @Schema(description = "Updated avatar image URL", example = "https://storage.example.com/avatars/jane_new.png")
    private String avatarUrl;
}
