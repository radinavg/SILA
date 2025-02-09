package com.inso.sila.endpoint;

import com.inso.sila.endpoint.dto.user.UserLoginDto;
import com.inso.sila.service.ApplicationUserService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(value = "/api/v1/authentication")
public class LoginEndpoint {
    private final ApplicationUserService userService;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public LoginEndpoint(ApplicationUserService userService) {
        this.userService = userService;
    }


    @PermitAll
    @PostMapping
    public String login(@RequestBody @Valid UserLoginDto userLoginDto) throws BadCredentialsException, LockedException {
        LOG.info("GET /api/v1/authentication/{}", userLoginDto);
        try {
            return userService.login(userLoginDto);
        } catch (BadCredentialsException e) {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            logClientError(status, "User does not exist", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (LockedException e) {
            HttpStatus status = HttpStatus.FORBIDDEN;
            logClientError(status, "User locked", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }


    private void logClientError(HttpStatus status, String message, Exception e) {
        LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
    }
}
