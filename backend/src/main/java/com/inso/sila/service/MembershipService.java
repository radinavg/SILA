package com.inso.sila.service;

import com.inso.sila.endpoint.dto.studio.membership.MembershipDto;

import java.util.List;

public interface MembershipService {

    MembershipDto addMembership(MembershipDto membershipDto);

    MembershipDto deleteMembership(Long id);

    List<MembershipDto> getMembershipsForUser();

    boolean hasMembershipForStudio(Long studioId);
}
