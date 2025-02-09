package com.inso.sila.endpoint.dto.studio.studio;

import com.inso.sila.endpoint.dto.image.ProfileImageDto;

import java.time.LocalDateTime;

public record StudioActivityListDto(
        Long studioActivityId,
        String name,
        ProfileImageDto profileImage,
        String description,
        LocalDateTime dateTime,
        float duration,
        float price
) {
}
