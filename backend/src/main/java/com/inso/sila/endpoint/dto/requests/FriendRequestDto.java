package com.inso.sila.endpoint.dto.requests;

import com.inso.sila.endpoint.dto.user.UserInfoDto;
import com.inso.sila.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FriendRequestDto(

        Long friendRequestId,

        @NotNull(message = "from must not be null.")
        UserInfoDto from,

        @NotNull(message = "to must not be null.")
        UserInfoDto to,

        @NotNull(message = "status must not be null.")
        RequestStatus status,

        @NotNull(message = "requestDateTime must not be null.")
        @PastOrPresent(message = "requestDateTime must be in the past or present.")
        LocalDateTime requestDateTime
) {
}
