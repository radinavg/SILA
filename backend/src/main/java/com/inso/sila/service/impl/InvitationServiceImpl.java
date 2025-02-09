package com.inso.sila.service.impl;

import com.inso.sila.endpoint.dto.requests.ActivityInvitationDto;
import com.inso.sila.endpoint.dto.requests.FriendRequestDto;
import com.inso.sila.endpoint.mapper.ActivityInvitationMapper;
import com.inso.sila.endpoint.mapper.FriendRequestMapper;
import com.inso.sila.endpoint.mapper.StudioActivityMapper;
import com.inso.sila.endpoint.mapper.UserMapper;
import com.inso.sila.entity.ActivityInvitation;
import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.FriendRequest;
import com.inso.sila.enums.RequestStatus;
import com.inso.sila.exception.ConflictException;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.repository.ActivityInvitationRepository;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.FriendRequestRepository;
import com.inso.sila.repository.StudioActivityRepository;
import com.inso.sila.security.UserAuthentication;
import com.inso.sila.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserAuthentication userAuthentication;
    private final ApplicationUserRepository applicationUserRepository;
    private final FriendRequestMapper friendRequestMapper;
    private final UserMapper userMapper;
    private final FriendRequestRepository friendRequestRepository;
    private final StudioActivityMapper studioActivityMapper;
    private final ActivityInvitationRepository activityInvitationRepository;
    private final ActivityInvitationMapper activityInvitationMapper;
    private final StudioActivityRepository studioActivityRepository;

    @Override
    public FriendRequestDto addFriendRequest(FriendRequestDto friendRequestDto) throws ConflictException {
        LOG.trace("addFriendRequest");

        // Get the 'from' user (authenticated user)
        ApplicationUser fromUser = applicationUserRepository.findByEmail(userAuthentication.getEmail());

        // Get the 'to' user
        ApplicationUser toUser = applicationUserRepository.findByEmail(friendRequestDto.to().email());
        if (toUser == null) {
            throw new NotFoundException("User not found with email: " + friendRequestDto.to().email());
        }

        // Check if a pending or accepted request already exists
        FriendRequest existingRequest = friendRequestRepository.findByFromAndTo(fromUser, toUser);
        if (existingRequest != null && (existingRequest.getStatus() == RequestStatus.PENDING || existingRequest.getStatus() == RequestStatus.ACCEPTED)) {
            throw new ConflictException("Friend request already exists between these users.", List.of());
        }

        // Create the FriendRequest object
        FriendRequest friendRequest = FriendRequest.builder()
                .from(fromUser)
                .to(toUser)
                .status(RequestStatus.PENDING)
                .requestDateTime(LocalDateTime.now())
                .build();

        // Save the FriendRequest to the repository
        friendRequestRepository.save(friendRequest);

        // Map the entity to DTO and return it
        return friendRequestMapper.entityToDto(friendRequest);
    }


    @Override
    public ActivityInvitationDto addActivityInvitation(ActivityInvitationDto activityInvitationDto) throws NotFoundException, ConflictException {
        LOG.trace("addActivityInvitation");

        // Get the 'to' user
        ApplicationUser toUser = applicationUserRepository.findByEmail(activityInvitationDto.to().email());
        if (toUser == null) {
            throw new NotFoundException("User not found with email: " + activityInvitationDto.to().email());
        }

        // Check if the activity exists
        if (activityInvitationDto.studioActivity() == null) {
            throw new NotFoundException("Studio Activity not found");
        }

        // Check if an invitation already exists for the same activity and users
        ActivityInvitation existingInvitation = activityInvitationRepository.findByFromAndToAndStudioActivity(
                applicationUserRepository.findByEmail(userAuthentication.getEmail()),
                toUser,
                studioActivityMapper.dtoToEntity(activityInvitationDto.studioActivity())
        );
        if (existingInvitation != null) {
            throw new ConflictException("Activity invitation already exists for these users and activity.", List.of());
        }

        // Create the ActivityInvitation object
        ActivityInvitation activityInvitation = ActivityInvitation.builder()
                .from(applicationUserRepository.findByEmail(userAuthentication.getEmail()))
                .to(toUser)
                .studioActivity(studioActivityMapper.dtoToEntity(activityInvitationDto.studioActivity()))
                .seen(false)
                .requestDateTime(LocalDateTime.now())
                .build();

        // Save the ActivityInvitation to the repository
        activityInvitationRepository.save(activityInvitation);

        // Map the entity to DTO and return it
        return activityInvitationMapper.entityToDto(activityInvitation);
    }


    @Override
    public List<FriendRequestDto> getUserSentFriendRequest(String email) {
        ApplicationUser from = applicationUserRepository.findByEmail(email);
        return friendRequestMapper.entityToDto(friendRequestRepository.findAllByFrom(from));
    }

    @Override
    public List<ActivityInvitationDto> getSentActivityInvitations(String email) {
        ApplicationUser from = applicationUserRepository.findByEmail(email);
        return activityInvitationMapper.entityToDtoList(activityInvitationRepository.findAllByFrom(from));
    }


    @Override
    public FriendRequestDto updateFriendRequest(FriendRequestDto friendRequestDto) {
        LOG.trace("updateFriendRequest({})", friendRequestDto);

        ApplicationUser fromUser = applicationUserRepository.findByEmail(friendRequestDto.from().email());

        ApplicationUser toUser = applicationUserRepository.findByEmail(userAuthentication.getEmail());

        if (fromUser == null || toUser == null) {
            throw new NotFoundException("User not found");
        }

        toUser.getFriends().add(fromUser);
        applicationUserRepository.save(toUser);

        fromUser.getFriends().add(toUser);
        applicationUserRepository.save(fromUser);


        FriendRequest friendRequest = friendRequestRepository.findById(friendRequestDto.friendRequestId())
                .orElseThrow(NotFoundException::new);
        friendRequest.setStatus(friendRequestDto.status());
        return friendRequestMapper.entityToDto(friendRequestRepository.save(friendRequest));
    }

}
