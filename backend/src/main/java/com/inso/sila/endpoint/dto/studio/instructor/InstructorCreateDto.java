package com.inso.sila.endpoint.dto.studio.instructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record InstructorCreateDto(
        @NotBlank(message = "Instructor firstname cannot be blank")
        @Size(max = 50, message = "Instructor firstname must not exceed 50 characters")
        String firstName,
        @NotBlank(message = "Instructor lastname cannot be blank")
        @Size(max = 50, message = "Instructor lastname must not exceed 50 characters")
        String lastName,
        MultipartFile profileImage) {
}
