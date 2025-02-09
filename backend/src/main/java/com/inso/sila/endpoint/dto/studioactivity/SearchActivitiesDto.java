package com.inso.sila.endpoint.dto.studioactivity;

public record SearchActivitiesDto(
        String activityType,
        Integer pageIndex,
        Integer pageSize
) {
}
