package com.inso.sila.endpoint;

import com.inso.sila.endpoint.dto.studio.membership.MembershipDto;
import com.inso.sila.exception.ConflictException;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.service.MembershipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/memberships")
public class MembershipEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MembershipService membershipService;

    public MembershipEndpoint(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @Secured("ROLE_USER")
    @PostMapping
    public ResponseEntity<MembershipDto> addMembership(@RequestBody MembershipDto membershipDto) {
        LOG.info("addMembership({})", membershipDto);

        MembershipDto createdMembership = membershipService.addMembership(membershipDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMembership);
    }

    @Secured("ROLE_USER")
    @GetMapping
    public ResponseEntity<List<MembershipDto>> getMembershipsForUser() {
        LOG.info("getMembershipsForUser()");
        List<MembershipDto> memberships = membershipService.getMembershipsForUser();
        return ResponseEntity.ok(memberships);
    }

    @Secured("ROLE_USER")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMembership(@PathVariable Long id) {
        LOG.info("deleteMembership({})", id);
        try {
            membershipService.deleteMembership(id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            LOG.warn("Membership not found: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Secured("ROLE_USER")
    @GetMapping("/hasMembershipForStudio/{id}")
    public ResponseEntity<Boolean> hasMembershipForStudio(@PathVariable Long id) {
        LOG.info("hasMembershipForStudio()");
        boolean hasMembership = membershipService.hasMembershipForStudio(id);
        return ResponseEntity.ok(hasMembership);
    }
}
