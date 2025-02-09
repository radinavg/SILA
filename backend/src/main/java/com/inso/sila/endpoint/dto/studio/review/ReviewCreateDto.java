package com.inso.sila.endpoint.dto.studio.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateDto(
        @NotBlank(message = "Review text must not be blank!")
        @Size(max = 1000, message = "Review text must not exceed 1000 characters.")
        String text,
        @NotNull(message = "Rating must be specified!")
        @Min(value = 1, message = "Review rating can be at lowest 1 star.")
        @Max(value = 5, message = "Review rating can be at most 5 stars.")
        Integer rating
) {
}
