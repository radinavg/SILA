package com.inso.sila.endpoint.dto.studioactivity;


import com.inso.sila.enums.SkillLevel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Builder
public record StudioActivityCreateDto(

        @NotBlank(message = "Studio activity name cannot be blank")
        @Size(max = 100, message = "Studio activity name must not exceed 100 characters")
        String name,

        @NotNull(message = "Studio activity image is missing")
        MultipartFile profileImageFile,

        @NotBlank(message = "Activity description cannot be blank")
        @Size(max = 500, message = "Activity description must not exceed 500 characters")
        String description,

        @Future(message = "Date and time of activity must be in future")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateTime,

        @Min(value = 0, message = "Duration of the cannot be negative")
        float duration,

        @Min(value = 0, message = "Price of activity cannot be negative")
        float price,

        @NotBlank(message = "Activity type is required")
        String type,

        @NotNull(message = "Activity must be attached to a studio")
        Long studioId,

        @Min(value = 1, message = "Capacity of an activity must be at least 1")
        int capacity,

        //@NotBlank(message = "Activity skill level cannot be blank")
        SkillLevel skillLevel,

        boolean equipment
) {

}
