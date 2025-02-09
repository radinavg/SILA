package com.inso.sila.endpoint.dto.user;

import com.inso.sila.endpoint.dto.requests.ActivityInvitationDto;
import com.inso.sila.endpoint.dto.requests.FriendRequestDto;
import com.inso.sila.enums.Gender;

import java.util.List;

public record UserDetailDto(
        String firstName,
        String lastName,
        String email,
        String location,
        Gender gender,
        Boolean isLocked,
        Boolean isAdmin,
        Boolean isStudioAdmin,
        String profileImagePath,
        List<FriendRequestDto> friendRequests,
        List<ActivityInvitationDto> activityInvitations
) {
}
