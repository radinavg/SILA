package com.inso.sila.endpoint.dto.user;

import com.inso.sila.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserInfoDto(
        @NotBlank(message = "First name cannot be blank")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,
        @NotBlank(message = "Last name cannot be blank")
        @Size(max = 255, message = "Last name must not exceed 255 characters")
        String lastName,
        String email,
        @NotBlank(message = "Location cannot be blank")
        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location,
        float longitude,
        float latitude,
        @NotNull(message = "Gender cannot be blank")
        Gender gender,
        String profileImagePath
) {
}
