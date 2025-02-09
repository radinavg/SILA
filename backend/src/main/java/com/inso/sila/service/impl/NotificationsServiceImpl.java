package com.inso.sila.service.impl;

import com.inso.sila.endpoint.dto.requests.NotificationsDto;
import com.inso.sila.endpoint.mapper.ActivityInvitationMapper;
import com.inso.sila.endpoint.mapper.FriendRequestMapper;
import com.inso.sila.repository.ActivityInvitationRepository;
import com.inso.sila.repository.FriendRequestRepository;
import com.inso.sila.security.UserAuthentication;
import com.inso.sila.service.NotificationsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationsServiceImpl implements NotificationsService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final FriendRequestRepository friendRequestRepository;
    private final ActivityInvitationRepository activityInvitationRepository;
    private final ActivityInvitationMapper activityInvitationMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final UserAuthentication userAuthentication;


    @Override
    public NotificationsDto getAllUnprocessedNotifications() {
        LOG.trace("getAllNotifications()");

        String email = userAuthentication.getEmail();

        return NotificationsDto.builder()
                .activityInvitations(activityInvitationMapper.entityToDtoList(activityInvitationRepository.findAllUnseenActivityInvitations(email)))
                .friendshipRequests(friendRequestMapper.entityToDto(friendRequestRepository.findAllPendingFriendRequests(email)))
                .build();
    }
}
