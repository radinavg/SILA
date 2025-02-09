package com.inso.sila.endpoint.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UserUpdatePasswordDto(
        String currentPassword,
        @NotNull(message = "Password field can't be null")
        @NotEmpty(message = "Password field can't be empty")
        @NotBlank(message = "Password field can't be empty")
        @Length(message = "Password must be between 8 and 16 characters long", min = 8, max = 16)
        String newPassword,
        @NotNull(message = "Password field can't be null")
        @NotEmpty(message = "Password field can't be empty")
        @NotBlank(message = "Password field can't be empty")
        @Length(message = "Password must be between 8 and 16 characters long", min = 8, max = 16)
        String confirmationPassword
) {
}