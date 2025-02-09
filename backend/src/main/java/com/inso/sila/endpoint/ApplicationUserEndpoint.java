package com.inso.sila.endpoint;

import com.inso.sila.endpoint.dto.studio.studio.StudioDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityDto;
import com.inso.sila.endpoint.dto.user.UpdateUserInfoDto;
import com.inso.sila.endpoint.dto.user.UserDetailDto;
import com.inso.sila.endpoint.dto.user.UserEmailDto;
import com.inso.sila.endpoint.dto.user.UserInfoDto;
import com.inso.sila.endpoint.dto.user.UserPreferencesDto;
import com.inso.sila.endpoint.dto.user.UserRegisterDto;
import com.inso.sila.endpoint.dto.user.UserSearchDto;
import com.inso.sila.endpoint.dto.user.UserUpdatePasswordDto;
import com.inso.sila.exception.ConflictException;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.exception.ValidationException;
import com.inso.sila.service.ApplicationUserService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/users")
public class ApplicationUserEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ApplicationUserService userService;
    private final WebClient webClient;
    private static final String CLUSTERING_API = "http://datascience:5000";

    public ApplicationUserEndpoint(ApplicationUserService userService, WebClient.Builder builder) {
        this.userService = userService;
        this.webClient = builder.baseUrl(CLUSTERING_API).build();
    }

    @PermitAll
    @PutMapping("reset/password")
    public ResponseEntity<UserEmailDto> resetUserPasswordAsUser(@RequestBody @Valid UserEmailDto resetUserPasswordDto) {
        LOG.info("PUT /api/v1/users/reset/user/password/{}", resetUserPasswordDto);
        try {
            var result = userService.resetUserPassword(resetUserPasswordDto.email());
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } catch (NotFoundException n) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Unknown user email", n);
            throw new ResponseStatusException(status, n.getMessage(), n);
        } catch (ValidationException e) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            logClientError(status, "Validation failure", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @PermitAll
    @PostMapping("/create")
    public ResponseEntity<UserEmailDto> createUser(@RequestBody @Valid UserRegisterDto userRegisterDto) {
        LOG.info("POST /api/v1/users/register");
        try {
            UserEmailDto userEmailDto = userService.registerUser(userRegisterDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(userEmailDto);
        } catch (ConflictException e) {
            HttpStatus status = HttpStatus.CONFLICT;
            logClientError(status, "User with this email already exists!", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (ValidationException e) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            logClientError(status, "Validation of user password failed", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }

    }

    @Secured("ROLE_USER")
    @GetMapping("/preferences-check")
    public ResponseEntity<Boolean> checkPreferences() {
        LOG.info("GET /api/v1/users/preferences-check");
        var result = userService.checkPreferences();
        return ResponseEntity.ok(result);
    }

    @Secured("ROLE_USER")
    @PostMapping("/create-preferences")
    public ResponseEntity<UserInfoDto> createPreferences(@RequestBody @Valid UserPreferencesDto userPreferencesDto) {
        LOG.info("POST /api/v1/users/create-preferences");
        try {
            UserInfoDto userEmailDto = userService.createUserPreferences(userPreferencesDto);
            String responseFromPython = webClient.get()
                    .uri("/on-preference")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            LOG.info("Response from Python service: {}", responseFromPython);
            return ResponseEntity.status(HttpStatus.CREATED).body(userEmailDto);
        } catch (ConflictException e) {
            HttpStatus status = HttpStatus.CONFLICT;
            logClientError(status, "Preferences already exist for this user!", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_USER")
    @PutMapping("/update-preferences")
    public ResponseEntity<UserInfoDto> updatePreferences(@RequestBody @Valid UserPreferencesDto userPreferencesDto) {
        LOG.info("POST /api/v1/users/update-preferences");
        UserInfoDto userEmailDto = userService.updateUserPreferences(userPreferencesDto);
        String responseFromPython = webClient.get()
                .uri("/on-preference")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        LOG.info("Response from Python service: {}", responseFromPython);
        return ResponseEntity.status(HttpStatus.OK).body(userEmailDto);

    }

    @Secured("ROLE_USER")
    @GetMapping("info/{email}")
    public UserInfoDto getUserInfo(@PathVariable("email") String email) {
        LOG.info("GET /api/v1/users/info/{}", email);
        try {
            return userService.getUserInfo(email);
        } catch (NotFoundException n) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Unknown user email", n);
            throw new ResponseStatusException(status, n.getMessage(), n);
        } catch (SecurityException s) {
            HttpStatus status = HttpStatus.FORBIDDEN;
            logClientError(status, "Fetch user info request denied", s);
            throw new ResponseStatusException(status, s.getMessage(), s);
        }
    }

    @Secured("ROLE_USER")
    @PutMapping("update")
    public UserInfoDto updateUserInfo(@RequestBody @Valid UpdateUserInfoDto updateUserInfoDto) {
        LOG.info("PUT /api/v1/users/update/{}", updateUserInfoDto);
        try {
            return userService.updateUserInfo(updateUserInfoDto);
        } catch (SecurityException s) {
            HttpStatus status = HttpStatus.FORBIDDEN;
            logClientError(status, "Update user info request denied", s);
            throw new ResponseStatusException(status, s.getMessage(), s);
        }
    }

    @Secured("ROLE_USER")
    @DeleteMapping("delete/{email}")
    public UserInfoDto deleteUser(@PathVariable("email") String email) {
        LOG.info("DELETE /api/v1/users/delete{}", email);
        try {
            return userService.deleteUser(email);
        } catch (NotFoundException n) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Unknown user email", n);
            throw new ResponseStatusException(status, n.getMessage(), n);
        } catch (SecurityException s) {
            HttpStatus status = HttpStatus.FORBIDDEN;
            logClientError(status, "Delete user request denied", s);
            throw new ResponseStatusException(status, s.getMessage(), s);
        } catch (ConflictException c) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            logClientError(status, "Conflict on deleting user", c);
            throw new ResponseStatusException(status, c.getMessage(), c);
        }
    }

    @Secured("ROLE_USER")
    @PutMapping("/upload-profile-image")
    public String uploadProfileImage(@RequestParam("file") MultipartFile file) {
        LOG.info("POST /api/v1/users/upload-profile-image");
        try {
            return userService.uploadUserProfileImage(file);
        } catch (ValidationException e) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            logClientError(status, "Error uploading profile image due to validation", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (IOException e) {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            logClientError(status, "Error uploading images", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_USER")
    @PutMapping("/update/password")
    public UserEmailDto updatePassword(@RequestBody @Valid UserUpdatePasswordDto userUpdatePasswordDto) {
        LOG.info("PUT /api/v1/users/update-password/{}", userUpdatePasswordDto);
        try {
            return userService.updateUserPassword(userUpdatePasswordDto);
        } catch (ValidationException e) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            logClientError(status, "Error updating password due to validation", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }

    }

    @Secured("ROLE_ADMIN")
    @GetMapping("search")
    public Page<UserDetailDto> searchUsers(UserSearchDto userSearchDto) {
        LOG.info("GET /api/v1/users/search?{}", userSearchDto);
        return userService.searchUsers(userSearchDto);
    }

    @Secured("ROLE_USER")
    @GetMapping("search-friends")
    public List<UserInfoDto> searchFriends(UserSearchDto userSearchDto) {
        LOG.info("GET /api/v1/users/searchFriends?{}", userSearchDto);
        return userService.searchFriends(userSearchDto);
    }

    @Secured("ROLE_USER")
    @GetMapping("search-my-friends")
    public List<UserInfoDto> searchMyFriends(UserSearchDto userSearchDto) {
        LOG.info("GET /api/v1/users/searchMyFriends?{}", userSearchDto);
        return userService.searchMyFriends(userSearchDto);
    }

    @Secured("ROLE_ADMIN")
    @PutMapping({"unblock/{email}"})
    public UserDetailDto unblockUser(@PathVariable("email") String email) {
        LOG.info("PUT /api/v1/users/unblock{}", email);
        try {
            return userService.unblockUser(email);
        } catch (ValidationException v) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            logClientError(status, "Validation unblock action failed", v);
            throw new ResponseStatusException(status, v.getMessage(), v);
        } catch (NotFoundException s) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "User to unblock doesn't exist", s);
            throw new ResponseStatusException(status, s.getMessage(), s);
        }
    }

    @PermitAll
    @GetMapping("/activities")
    public List<StudioActivityDto> getActivities() {
        LOG.info("GET /api/v1/activities/");
        return userService.getActivities();
    }

    @Secured("ROLE_USER")
    @GetMapping("/recommendations")
    public List<StudioActivityDto> getRecommendations() {
        LOG.info("GET /api/v1/recommendations/");
        return userService.getRecommendations();
    }

    @Secured("ROLE_USER")
    @GetMapping("/studio-recommendations")
    public List<StudioDto> getStudioRecommendations() {
        LOG.info("GET /api/v1/studio-recommendations/");
        return userService.getStudioRecommendations();
    }


    @Secured("ROLE_USER")
    @GetMapping("my-friends")
    public ResponseEntity<List<UserInfoDto>> getUserFriends(UserSearchDto userSearchDto) {
        LOG.info("GET /api/v1/users/my-friends");
        try {
            return ResponseEntity.ok().body(userService.searchMyFriends(userSearchDto));
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "User not found", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @Secured("ROLE_USER")
    @DeleteMapping("friend/{email}")
    public ResponseEntity<Void> deleteUserFriend(@PathVariable String email) {
        LOG.info("DELETE /api/v1/users/friend?email={}", email);
        try {
            userService.deleteUserFriend(email);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "User not found", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }


    private void logClientError(HttpStatus status, String message, Exception e) {
        LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
    }

}
