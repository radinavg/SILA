package com.inso.sila.endpoint.dto.studio.studio;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record StudioCreateDto(

        @NotBlank(message = "Studio name cannot be blank")
        @Size(max = 100, message = "Studio name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Description cannot be blank")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotBlank(message = "Location cannot be blank")
        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location,

        float longitude,
        float latitude,

        @NotNull(message = "Email field can't be null")
        @Email(message = "Email must be valid")
        @NotEmpty(message = "Email field can't be empty")
        @NotBlank(message = "Email field can't be empty")
        @Length(message = "Email must be less than 100 characters long", max = 100)
        String email,

        @NotNull(message = "Password field can't be null")
        @NotEmpty(message = "Password field can't be empty")
        @NotBlank(message = "Password field can't be empty")
        @Length(message = "Password must be between 8 and 16 characters long", min = 8, max = 16)
        String password,

        @NotNull(message = "Confirmation password field can't be null")
        @NotEmpty(message = "Confirmation password field can't be empty")
        @NotBlank(message = "Confirmation password field can't be empty")
        @Length(message = "Confirmation password must be between 8 and 16 characters long", min = 8, max = 16)
        String confirmPassword,

        @NotNull(message = "Profile image must be provided")
        MultipartFile profileImageFile
) {
}
