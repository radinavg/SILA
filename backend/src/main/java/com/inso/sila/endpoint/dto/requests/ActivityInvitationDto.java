package com.inso.sila.endpoint.dto.requests;

import com.inso.sila.endpoint.dto.studioactivity.StudioActivityDto;
import com.inso.sila.endpoint.dto.user.UserInfoDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ActivityInvitationDto(

        Long id,

        @NotNull(message = "from must not be null.")
        UserInfoDto from,

        @NotNull(message = "to must not be null.")
        UserInfoDto to,

        @NotNull(message = "studioActivity must not be null.")
        StudioActivityDto studioActivity,

        @NotNull(message = "seen must not be null.")
        Boolean seen,

        @NotNull(message = "request time must not be null.")
        @PastOrPresent(message = "request time must not be in the future")
        LocalDateTime requestDateTime
) {
}
