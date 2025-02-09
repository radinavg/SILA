package com.inso.sila.service.validation;

import com.inso.sila.endpoint.dto.user.UserUpdatePasswordDto;
import com.inso.sila.exception.ConflictException;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.exception.ValidationException;
import com.inso.sila.repository.ApplicationUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Component
public class ApplicationUserValidator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ApplicationUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ApplicationUserValidator(ApplicationUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public void validateExistingEmail(String email) throws NotFoundException {
        LOG.trace("validateExistingEmail({})", email);
        var result = userRepository.findByEmail(email);
        if (result == null) {
            throw new NotFoundException("User with the given email does not exist");
        }
    }

    public void checkIfUserExists(String email) throws ConflictException {
        LOG.trace("checkIfUserExists({})", email);
        List<String> conflictErrors = new ArrayList<>();
        if (userRepository.findByEmail(email) != null) {
            conflictErrors.add("User with the given email already exists");
        }
        if (!conflictErrors.isEmpty()) {
            throw new ConflictException("Couldn't create user", conflictErrors);
        }
    }

    public void validateAuthorization(String userEmail, String authorizedEmail) throws SecurityException {
        LOG.trace("validatingAuthorization{}{}", userEmail, authorizedEmail);
        var user = userRepository.findByEmail(userEmail);
        if (!user.getEmail().equals(authorizedEmail)) {
            LOG.warn("{} has tried to update {}[id: {}]", authorizedEmail, user.getEmail(), user.getApplicationUserId());
            throw new SecurityException("This action is not available");
        }

    }


    public void validateAuthorizationForDeleteUser(String userEmail, String authorizedEmail) throws SecurityException {
        LOG.trace("validatingAuthorizationForDeleteUser{}{}", userEmail, authorizedEmail);
        var user = userRepository.findByEmail(userEmail);
        var authorizedUser = userRepository.findByEmail(authorizedEmail);

        if (!authorizedUser.isAdmin() && !user.getEmail().equals(authorizedEmail)) {
            LOG.warn("{} has tried to update {}[id: {}]", authorizedEmail, user.getEmail(), user.getApplicationUserId());
            throw new SecurityException("This action is not available");
        }

    }

    public void checkForLastAdmin() throws ConflictException {
        LOG.trace("Check if current admin is last admin in the system...");
        var result = userRepository.findAdminsOrLockedAdmins(true, false);
        if (result.size() < 2) {
            throw new ConflictException("Conflict occurred", List.of("You are the last admin with access!"));
        }
    }


    public void validatePasswordForUpdate(UserUpdatePasswordDto userUpdatePasswordDto, String email) throws ValidationException {
        LOG.trace("validateUpdateUserPassword{}", email);

        List<String> validationErrors = new ArrayList<>();

        if (!passwordEncoder.matches(userUpdatePasswordDto.currentPassword(), userRepository.findByEmail(email).getPassword())) {
            validationErrors.add("Current password does not match expected one");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Failed to validate password", validationErrors);
        }
        validatePasswordSetting(userUpdatePasswordDto.newPassword(), userUpdatePasswordDto.confirmationPassword());
    }

    public void validatePasswordSetting(String password, String passwordConfirmation) throws ValidationException {
        List<String> validationErrors = new ArrayList<>();

        if (!password.matches("(?=.*[A-Z]).*")) {
            validationErrors.add("Password must contain at least one uppercase letter");
        }

        if (!password.matches("(?=.*\\d).*")) {
            validationErrors.add("Password must contain at least one number");
        }

        if (!passwordConfirmation.matches("(?=.*[.,\\-_!\"§$%&/()=?`*+\\\\]).*")) {
            validationErrors.add("Password must contain at least one special character");
        }

        if (!password.equals(passwordConfirmation)) {
            validationErrors.add("Passwords do not match");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed", validationErrors);
        }
    }

    public void validateOnUnblock(String email, String message) throws ValidationException, NotFoundException {
        LOG.trace("validateOnUnblock{}", email);

        List<String> validationErrors = new ArrayList<>();

        this.validateExistingEmail(email);
        if (!userRepository.findByEmail(email).isLocked()) {
            validationErrors.add("User " + email + " is not blocked");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(message, validationErrors);
        }
    }
}
