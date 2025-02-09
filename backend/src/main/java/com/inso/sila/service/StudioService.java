package com.inso.sila.service;

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
import org.springframework.web.multipart.MultipartFile;
import com.inso.sila.exception.ValidationException;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;

/**
 * Service interface for managing studio-related operations.
 */
public interface StudioService {

    /**
     * Adds a new studio.
     *
     * @param studio the DTO containing studio details
     * @return the saved Studio entity
     */
    CreatedStudioDto addStudio(StudioCreateDto studio) throws IOException, ValidationException, ConflictException;

    /**
     * Updates an existing studio.
     *
     * @param studio the DTO containing updated studio details
     * @return the updated Studio entity
     */
    StudioInfoDto updateApprovalOfStudio(StudioApprovalDto studio) throws NotFoundException;


    /**
     * Updates an existing studio.
     *
     * @param studioUpdateDto the DTO containing updated studio details.
     * @param studioId the studio id.
     * @return the updated Studio entity.
     */
    StudioDto updateStudio(StudioUpdateDto studioUpdateDto, Long studioId) throws NotFoundException, SecurityException;


    /**
     * Updates the profile image of a studio by a given studio id.
     *
     * @param profileImage the new profile image.
     * @param studioId the studio id.
     * @return the updated studio dto.
     */
    StudioDto updateProfileImage(MultipartFile profileImage, Long studioId) throws IOException, ValidationException, NotFoundException, SecurityException;


    /**
     * Retrieves all studios.
     *
     * @return a list of all Studio entities
     */
    List<StudioDto> getAllStudios();

    /**
     * Retrieves all liked studios by user.
     *
     * @return a list of liked Studio entities for one User
     */
    List<StudioDto> getAllLikedStudiosForUser();

    /**
     * Retrieves all studios that are not approved.
     *
     * @return a list of Studio entities that are not approved
     */
    List<StudioInfoDto> getNotApprovedStudios();

    /**
     * Deletes a studio by its ID.
     *
     * @param studioId the ID of the studio to delete
     * @return the deleted Studio entity
     * @throws jakarta.persistence.EntityNotFoundException if the studio is not found
     */
    StudioDto deleteStudio(Long studioId) throws NotFoundException;

    /**
     * Get a studio by given ID.
     *
     * @param studioId the ID of the needed studio
     * @return the needed studio
     * @throws jakarta.persistence.EntityNotFoundException if the studio is not found
     */
    StudioDto getStudioById(Long studioId) throws NotFoundException;

    /**
     * Add a frequently asked question to the studio.
     *
     * @param faqs the FAQ that needs to be added
     * @param studioId the ID of the needed studio
     * @return the added FAQ
     * @throws jakarta.persistence.EntityNotFoundException if the studio is not found
     */
    FaqsDto addFaq(FaqsDto faqs, Long studioId) throws SecurityException, NotFoundException;

    /**
     * Creates a new membership and associates it with the specified studio.
     *
     * @param membership the DTO containing membership details (e.g., name, duration, price).
     * @param studioId      the ID of the studio to link this membership to.
     * @return the saved Membership entity.
     * @throws NotFoundException if the studio or related activity is not found.
     */
    MembershipDto addMembership(MembershipDto membership, Long studioId) throws NotFoundException, SecurityException;


    /**
     * Adds an existent studio to the favourite
     * studios list of the current application user.
     *
     * @param studioId the id of the studio, which is added to the favourites.
     * @return a dto of the favourite studio
     * @throws NotFoundException if the studio or the current user are not found.
     */
    StudioDto addFavouriteStudio(Long studioId) throws NotFoundException;

    /**
     * Removes an existent studio from the favourite
     * studios list of the current application user.
     *
     * @param studioId the id of the studio, which is removed from the favourites.
     * @return a dto of the studio
     * @throws NotFoundException if the studio or the current user are not found.
     */
    StudioDto removeFavouriteStudio(Long studioId) throws NotFoundException;

    Page<StudioInfoDto> searchByNameAndLocation(StudioSearchDto studioSearchDto);

    Long getStudioIdByAdmin(String email);

    Boolean isCurrentUserAdmin(Long studioId) throws NotFoundException;

    InstructorDto addInstructor(InstructorCreateDto instructor, Long studioId) throws IOException, NotFoundException, ValidationException, SecurityException;

    /**
     * Adds new galleryImages to an existing studio with a studio id.
     *
     * @param studioId the studio id.
     * @param files the images as multipart files.
     * @return the studio dto with the newly created gallery images.
     */
    StudioDto addGalleryImages(Long studioId, MultipartFile[] files) throws IOException, NotFoundException, SecurityException, ValidationException;
}
