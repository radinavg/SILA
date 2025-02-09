package com.inso.sila.security;

import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.repository.ApplicationUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class UserAuthentication {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ApplicationUserRepository userRepository;

    public UserAuthentication(ApplicationUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public String getEmail() {
        return this.getAuthentication().getName();
    }

    public void checkBlockedStatus(String userEmail) throws SecurityException {
        LOG.trace("Checking blocked status for {}", userEmail);
        ApplicationUser user = userRepository.findByEmail(userEmail);

        if (user.isLocked()) {
            throw new SecurityException("User " + userEmail + " is blocked");
        }
    }

    public void checkAdminRequest(String currentlyLoggedIn, String targetEmail) throws SecurityException {
        LOG.trace("Checking admin {} request for {}", currentlyLoggedIn, targetEmail);
        if (currentlyLoggedIn == null) {
            throw new SecurityException("Unknown user trying to access admin request");
        }

        if (currentlyLoggedIn.equals(targetEmail)) {
            throw new SecurityException(currentlyLoggedIn + " is an admin trying to block " + targetEmail);
        }
    }
}