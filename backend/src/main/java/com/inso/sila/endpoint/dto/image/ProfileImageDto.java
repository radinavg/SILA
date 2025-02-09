package com.inso.sila.endpoint.dto.image;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ProfileImageDto(

        @NotBlank(message = "Profile image name must not be blank")
        String name,
        @NotBlank(message = "Profile image path must not be blank")
        String path
) {
}
