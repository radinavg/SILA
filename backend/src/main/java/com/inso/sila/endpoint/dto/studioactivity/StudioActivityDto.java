package com.inso.sila.endpoint.dto.studioactivity;

import com.inso.sila.endpoint.dto.image.ProfileImageDto;
import com.inso.sila.endpoint.dto.studio.instructor.InstructorDto;
import com.inso.sila.endpoint.dto.studio.membership.MembershipDto;
import com.inso.sila.endpoint.dto.user.UserInfoDto;
import com.inso.sila.endpoint.dto.user.UserRegisterDto;
import com.inso.sila.enums.ActivityType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;


import java.time.LocalDateTime;
import java.util.List;

@Builder
public record StudioActivityDto(
        @NotNull(message = "Studio activity must be attached to a studio")
        Long studioActivityId,
        ProfileImageDto profileImage,
        @NotBlank(message = "Studio activity name cannot be blank")
        @Size(max = 100, message = "Studio activity name must not exceed 100 characters")
        String name,
        @NotBlank(message = "Activity description cannot be blank")
        @Size(max = 500, message = "Activity description must not exceed 500 characters")
        String description,
        @Future(message = "Date and time of activity must be in future")
        LocalDateTime dateTime,
        @Min(value = 0, message = "Duration of activity cannot be negative")
        float duration,
        @Min(value = 0, message = "Price of activity cannot be negative")
        float price,
        @NotNull(message = "Activity type is required")
        ActivityType type,
        int capacity,
        List<UserInfoDto> applicationUsers,
        MembershipDto membership,
        InstructorDto instructor
) {
}
