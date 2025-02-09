package com.inso.sila.endpoint;


import com.inso.sila.endpoint.dto.requests.ActivityInvitationDto;
import com.inso.sila.endpoint.dto.requests.FriendRequestDto;
import com.inso.sila.exception.ConflictException;
import com.inso.sila.service.InvitationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/invitation")
public class InvitationEndPoint {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final InvitationService invitationService;

    public InvitationEndPoint(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @Secured("ROLE_USER")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FriendRequestDto addFriendRequest(@RequestBody FriendRequestDto friendRequestDto) throws ConflictException {
        LOG.info("Adding friend request: {}", friendRequestDto);
        return invitationService.addFriendRequest(friendRequestDto);
    }

    @Secured("ROLE_USER")
    @PostMapping("/activityInvitation")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityInvitationDto addActivityInvitation(@RequestBody ActivityInvitationDto activityInvitationDto) throws ConflictException {
        LOG.info("Adding activity invitation: {}", activityInvitationDto);
        return invitationService.addActivityInvitation(activityInvitationDto);
    }


    @Secured("ROLE_USER")
    @GetMapping("/my_friend_invitations/{email}")
    public List<FriendRequestDto> getUserSentFriendRequests(@PathVariable("email") String email) {
        LOG.info("Getting sent friend requests for email: {}", email);
        return invitationService.getUserSentFriendRequest(email);
    }

    @Secured("ROLE_USER")
    @GetMapping("/my_activity_invitations/{email}")
    public List<ActivityInvitationDto> getUserSentActivityInvitations(@PathVariable("email") String email) {
        LOG.info("Getting activity invitations for email: {}", email);
        return invitationService.getSentActivityInvitations(email);
    }


    @Secured("ROLE_USER")
    @PutMapping
    public ResponseEntity<FriendRequestDto> updateFriendRequest(@RequestBody FriendRequestDto friendRequestDto) {
        LOG.info("Updating friend request: {}", friendRequestDto);
        return ResponseEntity.ok(invitationService.updateFriendRequest(friendRequestDto));
    }



}
