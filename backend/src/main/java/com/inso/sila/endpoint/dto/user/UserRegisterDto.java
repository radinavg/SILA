package com.inso.sila.endpoint.dto.user;

import com.inso.sila.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record UserRegisterDto(

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
        String passwordConfirmation,

        @NotBlank(message = "First name cannot be blank")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,

        @NotNull(message = "Gender cannot be blank")
        Gender gender,

        @NotBlank(message = "Location cannot be blank")
        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location,

        float longitude,
        float latitude
) {
}
