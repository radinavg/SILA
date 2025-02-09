package com.inso.sila.endpoint.dto.studio.studio;

public record StudioInfoDto(
        Integer studioId,
        String name,
        String description,
        String location,
        String email,
        String profileImagePath
){
}
