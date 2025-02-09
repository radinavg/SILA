package com.inso.sila.endpoint.dto.studio.instructor;

import com.inso.sila.endpoint.dto.image.ProfileImageDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityDto;

import java.util.List;

public record InstructorDto(
       Long instructorId,
       String firstName,
       String lastName,
       List<StudioActivityDto> studioActivities,
       ProfileImageDto profileImage
) {
}
