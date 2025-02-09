package com.inso.sila.endpoint.dto.studio.studio;

import com.inso.sila.endpoint.dto.image.ProfileImageDto;
import com.inso.sila.endpoint.dto.studio.instructor.InstructorDto;
import lombok.Builder;

import java.util.List;

@Builder
public record StudioForActivityDto(
        Long studioId,
        ProfileImageDto profileImage,
        String name,
        String description,
        String location,
        float longitude,
        float latitude,
        List<InstructorDto> instructors


) {
}
