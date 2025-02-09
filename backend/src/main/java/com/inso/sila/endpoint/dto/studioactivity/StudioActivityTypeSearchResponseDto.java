package com.inso.sila.endpoint.dto.studioactivity;

import java.time.LocalDateTime;

public record StudioActivityTypeSearchResponseDto(
        Long studioActivityId,
        String name,
        String description,
        LocalDateTime dateTime,
        float duration,
        float price,
        String profileImagePath
) {
}
