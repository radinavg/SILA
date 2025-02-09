package com.inso.sila.service.impl;

import com.inso.sila.endpoint.dto.studio.membership.MembershipDto;
import com.inso.sila.endpoint.mapper.MembershipMapper;
import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.Membership;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.MembershipRepository;
import com.inso.sila.security.UserAuthentication;
import com.inso.sila.service.MembershipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MembershipServiceImpl implements MembershipService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ApplicationUserRepository applicationUserRepository;
    private final UserAuthentication userAuthentication;
    private final MembershipRepository membershipRepository;
    private final MembershipMapper membershipMapper;

    public MembershipServiceImpl(ApplicationUserRepository applicationUserRepository,
                                 UserAuthentication userAuthentication,
                                 MembershipRepository membershipRepository,
                                 MembershipMapper membershipMapper) {
        this.applicationUserRepository = applicationUserRepository;
        this.userAuthentication = userAuthentication;
        this.membershipRepository = membershipRepository;
        this.membershipMapper = membershipMapper;
    }

    @Override
    public MembershipDto addMembership(MembershipDto membershipDto) {
        LOG.trace("addMembership({})", membershipDto);

        ApplicationUser user = applicationUserRepository.findByEmail(userAuthentication.getEmail());
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        Membership membership = membershipRepository.findById(membershipDto.membershipId())
                .orElseThrow(() -> new NotFoundException("Membership not found"));

        membership.getApplicationUsers().add(user);
        user.getMemberships().add(membership);

        membershipRepository.save(membership);
        applicationUserRepository.save(user);

        return membershipMapper.entityToDto(membership);
    }

    @Override
    public MembershipDto deleteMembership(Long id) {
        LOG.trace("unsubscribeFromMembership({})", id);

        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Membership not found"));

        ApplicationUser user = applicationUserRepository.findByEmail(userAuthentication.getEmail());

        if (!membership.getApplicationUsers().contains(user)) {
            throw new NotFoundException("This membership does not belong to the user");
        }

        membership.getApplicationUsers().remove(user);
        user.getMemberships().remove(membership);

        membershipRepository.save(membership);
        applicationUserRepository.save(user);

        return membershipMapper.entityToDto(membership);
    }

    @Override
    public List<MembershipDto> getMembershipsForUser() {
        LOG.trace("getMembershipsForUser()");

        ApplicationUser user = applicationUserRepository.findByEmail(userAuthentication.getEmail());
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        return user.getMemberships().stream()
                .map(membershipMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasMembershipForStudio(Long studioId) {
        ApplicationUser user = applicationUserRepository.findByEmail(userAuthentication.getEmail());
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        return user.getMemberships().stream()
                .anyMatch(membership -> membership.getStudio().getStudioId().equals(studioId));
    }

}
