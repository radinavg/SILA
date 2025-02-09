package com.inso.sila.service;

import com.inso.sila.endpoint.dto.studio.studio.StudioActivityListDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioForActivityDto;
import com.inso.sila.endpoint.dto.studioactivity.SearchActivitiesDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityCreateDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityPreferencesDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityTypeSearchResponseDto;
import com.inso.sila.endpoint.dto.user.UserInfoDto;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.exception.ValidationException;
import org.springframework.data.domain.Page;
import com.inso.sila.exception.ConflictException;

import java.io.IOException;

import java.util.List;

public interface StudioActivityService {

    /**
     * Finds a studio activity by a given id.
     *
     * @param id the id of the studio activity.
     * @return a studio activity dto.
     * @throws NotFoundException if there is no studio activity with this id.
     */
    StudioActivityDto findById(Long id) throws NotFoundException;

    /**
     * Creates a new studio activity by a given dto.
     *
     * @param studioActivityCreateDto a studio activity dto with all fields needed for the creation.
     * @return a studio activity dto
     * @throws IOException if an error occurred while saving the profile image of the studio activity.
     */
    StudioActivityListDto save(StudioActivityCreateDto studioActivityCreateDto) throws IOException, SecurityException, ValidationException;

    StudioActivityDto update(StudioActivityDto studioActivityDto) throws NotFoundException, SecurityException;

    void delete(Long id) throws NotFoundException, SecurityException;

    List<String> getActivityTypes();

    Page<StudioActivityTypeSearchResponseDto> getActivitiesByType(SearchActivitiesDto type);

    StudioForActivityDto getStudioForActivity(Long activityId);

    StudioActivityDto bookAnActivity(StudioActivityDto studioActivityDto) throws ConflictException;

    void unbookAnActivity(StudioActivityDto studioActivityDto) throws ConflictException;

    List<StudioActivityListDto> getActivities(Long studioId) throws NotFoundException;

    List<UserInfoDto> getParticipatingFriends(Long activityId);

    boolean alreadyBooked(Long activityId);

    /**
     * Creates studio activity preferences.
     *
     * @param studioActivityPreferencesDto dto of the studio activity preference.
     * @return a studio activity dto.
     */
    StudioActivityPreferencesDto createStudioActivityPreferences(StudioActivityPreferencesDto studioActivityPreferencesDto);

    /**
     * Updates studio activity preferences.
     *
     * @param studioActivityPreferencesDto dto of the studio activity preference.
     * @return a studio activity preferences dto.
     */
    StudioActivityPreferencesDto updateStudioActivityPreferences(StudioActivityPreferencesDto studioActivityPreferencesDto);
}
