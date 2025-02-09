package com.inso.sila.endpoint.dto.image;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record GalleryImageDto(

        @NotBlank(message = "Gallery image name must not be blank")
        String name,
        @NotBlank(message = "Gallery image path must not be blank")
        String path
) {
}
