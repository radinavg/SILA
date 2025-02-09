package com.inso.sila.endpoint.dto.studio.studio;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudioUpdateDto(
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
        float latitude
) {
}
