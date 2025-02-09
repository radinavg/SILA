package com.inso.sila.service;

import com.inso.sila.endpoint.dto.requests.ActivityInvitationDto;
import com.inso.sila.endpoint.dto.requests.FriendRequestDto;
import com.inso.sila.entity.FriendRequest;
import com.inso.sila.exception.ConflictException;
import com.inso.sila.exception.NotFoundException;
import org.aspectj.weaver.ast.Not;

import java.util.List;

public interface InvitationService {

    FriendRequestDto addFriendRequest(FriendRequestDto friendRequestDto) throws ConflictException;

    ActivityInvitationDto addActivityInvitation(ActivityInvitationDto activityInvitationDto) throws ConflictException;

    FriendRequestDto updateFriendRequest(FriendRequestDto friendRequestDto);

    List<FriendRequestDto> getUserSentFriendRequest(String email);

    List<ActivityInvitationDto> getSentActivityInvitations(String email);
}
