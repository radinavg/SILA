package com.inso.sila.endpoint;

import com.inso.sila.endpoint.dto.studio.studio.CreatedStudioDto;
import com.inso.sila.endpoint.dto.studio.faqs.FaqsDto;
import com.inso.sila.endpoint.dto.studio.instructor.InstructorCreateDto;
import com.inso.sila.endpoint.dto.studio.instructor.InstructorDto;
import com.inso.sila.endpoint.dto.studio.membership.MembershipDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioApprovalDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioCreateDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioUpdateDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioInfoDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioSearchDto;
import com.inso.sila.exception.ConflictException;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.exception.ValidationException;
import com.inso.sila.service.StudioService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/studios")
@RequiredArgsConstructor
public class StudioEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final StudioService studioService;

    @PermitAll
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreatedStudioDto> addStudio(@ModelAttribute(value = "studioCreateDto") @Valid StudioCreateDto studioCreateDto) {
        LOG.info("Adding new studio: {}", studioCreateDto.name());
        try {
            CreatedStudioDto createdStudio = studioService.addStudio(studioCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudio);
        } catch (IOException e) {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            logClientError(status, "Error uploading studio profile image", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (ValidationException e) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            logClientError(status, "Validation of studio and admin to create failed", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (ConflictException e) {
            HttpStatus status = HttpStatus.CONFLICT;
            logClientError(status, "User with this email already exists!", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @PutMapping("/approve")
    @Secured("ROLE_ADMIN")
    public StudioInfoDto approveStudio(@RequestBody StudioApprovalDto studio) {
        LOG.info("Updating studio with ID: {}", studio.studioId());
        try {
            return studioService.updateApprovalOfStudio(studio);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Unknown studio to approve", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_STUDIO_ADMIN")
    @PutMapping(value = "/{studioId}")
    public StudioDto updateStudio(@RequestBody @Valid StudioUpdateDto studioUpdateDto, @PathVariable Long studioId) {
        LOG.info("Updating studio with ID: {}", studioId);
        try {
            return studioService.updateStudio(studioUpdateDto, studioId);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Unknown studio to update", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (SecurityException e) {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            logClientError(status, "Action not allowed", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }


    @Secured("ROLE_STUDIO_ADMIN")
    @PutMapping(value = "/{studioId}/update-profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StudioDto updateProfileImage(@PathVariable Long studioId, @RequestParam("file") MultipartFile file) {
        LOG.info("Updating profile image with ID: {}", studioId);
        try {
            return studioService.updateProfileImage(file, studioId);
        } catch (ValidationException e) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            logClientError(status, "Error uploading profile image due to validation", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (IOException e) {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            logClientError(status, "Error uploading images", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Unknown studio to update", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (SecurityException e) {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            logClientError(status, "Action not allowed", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @GetMapping
    @Secured("ROLE_USER")
    public List<StudioDto> getAllStudios() {
        LOG.info("Fetching all studios");
        return studioService.getAllStudios();
    }

    @GetMapping("/notApproved")
    @Secured("ROLE_ADMIN")
    public List<StudioInfoDto> getNotApprovedStudios() {
        LOG.info("Fetching all not approved studios");
        return studioService.getNotApprovedStudios();
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_ADMIN")
    public StudioDto deleteStudio(@PathVariable Long id) {
        LOG.info("Deleting studio with ID: {}", id);
        try {
            return studioService.deleteStudio(id);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Unknown studio to delete", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @GetMapping("/{id}")
    @Secured("ROLE_USER")
    public StudioDto getStudioById(@PathVariable Long id) {
        LOG.info("Fetching studio with ID: {}", id);
        try {
            return studioService.getStudioById(id);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Studio not found", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_STUDIO_ADMIN")
    @PostMapping("/{studioId}/faqs")
    public ResponseEntity<FaqsDto> addFaq(@PathVariable Long studioId, @RequestBody @Valid FaqsDto faq) {
        LOG.info("Add FAQs for studio with ID: {}", studioId);
        try {
            FaqsDto faqs = studioService.addFaq(faq, studioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(faqs);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Studio not found", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (SecurityException s) {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            logClientError(status, "Adding FAQ denied", s);
            throw new ResponseStatusException(status, s.getMessage(), s);
        }
    }

    @Secured("ROLE_STUDIO_ADMIN")
    @PostMapping("/{studioId}/memberships")
    public ResponseEntity<MembershipDto> addMembership(@PathVariable Long studioId, @RequestBody @Valid MembershipDto membership) {
        LOG.info("Add Membership for studio with ID: {}", studioId);
        try {
            MembershipDto newMembership = studioService.addMembership(membership, studioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(newMembership);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Studio not found", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (SecurityException s) {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            logClientError(status, "Adding membership denied, you are not admin of this studio", s);
            throw new ResponseStatusException(status, s.getMessage(), s);
        }
    }

    @Secured("ROLE_USER")
    @GetMapping("/favourite")
    public ResponseEntity<List<StudioDto>> getFavouriteStudios() {
        LOG.info("Fetching all favourite studios of this user");
        List<StudioDto> favouriteStudios = studioService.getAllLikedStudiosForUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(favouriteStudios);
    }

    @Secured("ROLE_USER")
    @PostMapping("/{studioId}/add-favourite")
    public ResponseEntity<StudioDto> addFavouriteStudio(@PathVariable Long studioId) {
        LOG.info("Add favourite studio with id: {}", studioId);
        try {
            StudioDto studioDto = studioService.addFavouriteStudio(studioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(studioDto);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Studio not found", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_USER")
    @DeleteMapping("/{studioId}/remove-favourite")
    public StudioDto removeFavouriteStudio(@PathVariable Long studioId) {
        LOG.info("Remove favourite studio with id: {}", studioId);
        try {
            return studioService.removeFavouriteStudio(studioId);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Studio not found", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_USER")
    @GetMapping("/search")
    public ResponseEntity<Page<StudioInfoDto>> searchByNameAndLocation(StudioSearchDto studioSearchDto) {
        LOG.info("Searching studios by name: {} and location: {}", studioSearchDto.name(), studioSearchDto.location());
        Page<StudioInfoDto> studios = studioService.searchByNameAndLocation(studioSearchDto);
        return ResponseEntity.status(HttpStatus.OK).body(studios);
    }

    @Secured("ROLE_STUDIO_ADMIN")
    @GetMapping("/getStudioByAdmin/{email}")
    public Long getStudioIdByAdmin(@PathVariable String email) {
        return studioService.getStudioIdByAdmin(email);
    }

    @Secured("ROLE_USER")
    @GetMapping("/isAdmin/{studioId}")
    public boolean isAdmin(@PathVariable Long studioId) {
        try {
            return studioService.isCurrentUserAdmin(studioId);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Studio not found", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_STUDIO_ADMIN")
    @PostMapping(value = "/add-instructor/{studioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InstructorDto> addInstructor(@PathVariable Long studioId, @ModelAttribute @Valid InstructorCreateDto instructor) {
        LOG.info("Add Instructor: {}", instructor);
        try {
            InstructorDto created = studioService.addInstructor(instructor, studioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IOException e) {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            logClientError(status, "Error uploading studio profile image", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (ValidationException e) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            logClientError(status, "Validation of instructor profile image", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Studio to add instructor to not found", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }  catch (SecurityException e) {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            logClientError(status, "Action not allowed", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_STUDIO_ADMIN")
    @PutMapping("/{studioId}/add-gallery-images")
    public ResponseEntity<StudioDto> addGalleryImages(@PathVariable Long studioId, @ModelAttribute("files") MultipartFile[] files) {
        LOG.info("Add gallery images for studio with ID: {}", studioId);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(this.studioService.addGalleryImages(studioId, files));
        } catch (IOException e) {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            logClientError(status, "Error uploading gallery images", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (ValidationException e) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            logClientError(status, "Validation of images failed", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Studio to add images to not found", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }  catch (SecurityException e) {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            logClientError(status, "Action not allowed", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    private void logClientError(HttpStatus status, String message, Exception e) {
        LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
    }
}
