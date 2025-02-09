package com.inso.sila.endpoint;

import com.inso.sila.endpoint.dto.studio.studio.StudioActivityListDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioForActivityDto;
import com.inso.sila.endpoint.dto.studioactivity.SearchActivitiesDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityCreateDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityTypeSearchResponseDto;
import com.inso.sila.endpoint.dto.user.UserInfoDto;
import com.inso.sila.exception.ConflictException;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.exception.ValidationException;
import com.inso.sila.service.StudioActivityService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/studio-activities")
public class StudioActivityEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final StudioActivityService studioActivityService;


    public StudioActivityEndpoint(StudioActivityService studioActivityService) {
        this.studioActivityService = studioActivityService;
    }

    @Secured("ROLE_USER")

    @GetMapping("{id}")
    public StudioActivityDto findById(@PathVariable Long id) {
        LOG.info("Find studio activity by id: {}", id);
        try {
            return studioActivityService.findById(id);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Activity not found", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_STUDIO_ADMIN")
    @PutMapping()
    public StudioActivityDto updateById(@RequestBody @Valid StudioActivityDto studioActivityDto) {
        LOG.info("Update studio activity: {}", studioActivityDto);
        try {
            return studioActivityService.update(studioActivityDto);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Activity to update not found", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (SecurityException s) {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            logClientError(status, "Updating activity denied, you are not admin of this studio", s);
            throw new ResponseStatusException(status, s.getMessage(), s);
        }
    }

    @Secured("ROLE_STUDIO_ADMIN")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<StudioActivityListDto> save(@ModelAttribute(value = "studioActivityCreateDto") @Valid StudioActivityCreateDto studioActivityCreateDto) {
        LOG.info("Save studio activity: {}", studioActivityCreateDto);
        try {
            StudioActivityListDto activity = studioActivityService.save(studioActivityCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(activity);
        } catch (IOException e) {
            logClientError(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving studio activity", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        } catch (NotFoundException e) {
            logClientError(HttpStatus.NOT_FOUND, "Studio for which you are trying to save activity not found", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (SecurityException s) {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            logClientError(status, "Creating activity denied, you are not admin of this studio", s);
            throw new ResponseStatusException(status, s.getMessage(), s);
        } catch (ValidationException e) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            logClientError(status, "Validation error of image file", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_STUDIO_ADMIN")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.info("Delete studio activity with id: {}", id);
        try {
            studioActivityService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Activity to delete not found", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (SecurityException s) {
            HttpStatus status = HttpStatus.FORBIDDEN;
            logClientError(status, "Deleting activity denied, you are not admin of this studio", s);
            throw new ResponseStatusException(status, s.getMessage(), s);
        }
    }

    @Secured("ROLE_USER")
    @GetMapping("activityId/{id}")
    public StudioForActivityDto findStudioByActivityId(@PathVariable Long id) {
        LOG.info("Find studio by activity id: {}", id);
        try {
            return studioActivityService.getStudioForActivity(id);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Studio not found", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_USER")
    @PostMapping("book")
    public ResponseEntity<StudioActivityDto> bookAnActivity(@RequestBody StudioActivityDto studioActivityDto) {
        LOG.info("bookAnActivity({})", studioActivityDto);
        try {
            StudioActivityDto activityDto = studioActivityService.bookAnActivity(studioActivityDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(activityDto);
        } catch (ConflictException e) {
            HttpStatus status = HttpStatus.CONFLICT;
            logClientError(status, "Conflict: Activity already booked", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_USER")
    @PostMapping("unbook")
    public ResponseEntity<Boolean> unBookAnActivity(@RequestBody StudioActivityDto studioActivityDto) {
        LOG.info("unBookAnActivity({})", studioActivityDto);
        try {
            studioActivityService.unbookAnActivity(studioActivityDto);
            return ResponseEntity.ok(true);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Studio not found", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (ConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }

    @Secured("ROLE_USER")
    @GetMapping("/types")
    public ResponseEntity<List<String>> getActivityTypes() {
        LOG.info("getActivityTypes()");
        var result = studioActivityService.getActivityTypes();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Secured("ROLE_USER")
    @GetMapping("/exploreTypes")
    public ResponseEntity<Page<StudioActivityTypeSearchResponseDto>> getActivitiesByType(SearchActivitiesDto type) {
        LOG.info("getActivitiesByType()");
        var result = studioActivityService.getActivitiesByType(type);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Secured("ROLE_USER")
    @GetMapping("/studio/{studioId}/activities")
    public List<StudioActivityListDto> getActivities(@PathVariable Long studioId) {
        LOG.info("Get studio activities from studio: {}", studioId);
        try {
            return studioActivityService.getActivities(studioId);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Studio not found", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_USER")
    @GetMapping("get_participated_friends/{id}")
    public List<UserInfoDto> getParticipatedFriends(@PathVariable Long id) {
        LOG.info("getParticipatedFriends");
        return studioActivityService.getParticipatingFriends(id);
    }

    @Secured("ROLE_USER")
    @GetMapping("is_already_booked/{id}")
    public ResponseEntity<Boolean> isAlreadyBooked(@PathVariable Long id) {
        LOG.info("isAlreadyBooked");
        try {
            boolean isBooked = this.studioActivityService.alreadyBooked(id);
            return ResponseEntity.ok(isBooked); // Return 200 with the result
        } catch (Exception e) {
            LOG.error("Error checking booking status for activity ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false); // Return 500 if there's an error
        }
    }

    private void logClientError(HttpStatus status, String message, Exception e) {
        LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
    }
}
