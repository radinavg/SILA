package com.inso.sila.endpoint.dto.studio.studio;

public record StudioSearchDto(
        String name,
        String location,
        Integer pageIndex,
        Integer pageSize
) {
}
