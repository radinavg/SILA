package com.inso.sila.endpoint.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UserEmailDto(
        @NotNull(message = "Email field can't be null")
        @Email(message = "Email must be valid")
        @NotEmpty(message = "Email field can't be empty")
        @NotBlank(message = "Email field can't be empty")
        @Length(message = "Email must be less than 100 characters long", max = 100)
        String email
) {
}