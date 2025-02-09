package com.inso.sila.endpoint.dto.requests;

import lombok.Builder;

import java.util.List;

@Builder
public record NotificationsDto(
        List<FriendRequestDto> friendshipRequests,
        List<ActivityInvitationDto> activityInvitations
) {
}
