package com.inso.sila.service.impl;

import com.inso.sila.endpoint.dto.studio.studio.StudioActivityListDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioForActivityDto;
import com.inso.sila.endpoint.dto.studioactivity.SearchActivitiesDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityCreateDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityPreferencesDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityTypeSearchResponseDto;
import com.inso.sila.endpoint.dto.user.UserInfoDto;
import com.inso.sila.endpoint.mapper.StudioActivityMapper;
import com.inso.sila.endpoint.mapper.StudioActivityPreferencesMapper;
import com.inso.sila.endpoint.mapper.StudioMapper;
import com.inso.sila.endpoint.mapper.UserMapper;
import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.ActivityTypeAttributes;
import com.inso.sila.entity.ProfileImage;
import com.inso.sila.entity.Studio;
import com.inso.sila.entity.StudioActivity;
import com.inso.sila.entity.StudioActivityPreferences;
import com.inso.sila.enums.ActivityType;
import com.inso.sila.enums.SkillLevel;
import com.inso.sila.exception.ConflictException;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.exception.ValidationException;
import com.inso.sila.repository.ActivityTypeAttributesRepository;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.ProfileImageRepository;
import com.inso.sila.repository.StudioActivityPreferencesRepository;
import com.inso.sila.repository.StudioActivityRepository;
import com.inso.sila.repository.StudioRepository;
import com.inso.sila.security.UserAuthentication;
import com.inso.sila.service.ImageService;
import com.inso.sila.service.StudioActivityService;
import com.inso.sila.service.validation.ApplicationUserValidator;
import com.inso.sila.service.validation.ImageValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Service
public class StudioActivityServiceImpl implements StudioActivityService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String PROFILE_IMAGES_DIR = "assets/studio-activity/profile-images/";

    private final StudioActivityMapper studioActivityMapper;
    private final ImageService imageService;
    private final StudioRepository studioRepository;
    private final StudioActivityRepository studioActivityRepository;
    private final StudioActivityPreferencesRepository studioActivityPreferencesRepository;
    private final ProfileImageRepository profileImageRepository;
    private final StudioMapper studioMapper;
    private final UserAuthentication userAuthentication;
    private final ApplicationUserRepository applicationUserRepository;
    private final ApplicationUserValidator applicationUserValidator;
    private final ImageValidator imageValidator;
    private final UserMapper userMapper;
    private final StudioActivityPreferencesMapper studioActivityPreferencesMapper;
    private final StudioActivityPreferencesRepository preferencesRepository;
    private final ActivityTypeAttributesRepository attributesRepository;
    private final WebClient webClient;
    private static final String DATASCIENCE_API = "http://datascience:5000";

    public StudioActivityServiceImpl(StudioActivityMapper studioActivityMapper, ImageService imageService,
                                     StudioRepository studioRepository, StudioActivityRepository studioActivityRepository,
                                     StudioActivityPreferencesRepository studioActivityPreferencesRepository,
                                     ProfileImageRepository profileImageRepository, StudioMapper studioMapper,
                                     UserAuthentication userAuthentication, ApplicationUserRepository applicationUserRepository,
                                     ApplicationUserValidator applicationUserValidator, ImageValidator imageValidator,
                                     UserMapper userMapper, StudioActivityPreferencesMapper studioActivityPreferencesMapper,
                                     StudioActivityPreferencesRepository preferencesRepository, ActivityTypeAttributesRepository attributesRepository,
                                     WebClient.Builder builder) {
        this.studioActivityMapper = studioActivityMapper;
        this.imageService = imageService;
        this.studioRepository = studioRepository;
        this.studioActivityRepository = studioActivityRepository;
        this.studioActivityPreferencesRepository = studioActivityPreferencesRepository;
        this.profileImageRepository = profileImageRepository;
        this.studioMapper = studioMapper;
        this.userAuthentication = userAuthentication;
        this.applicationUserRepository = applicationUserRepository;
        this.applicationUserValidator = applicationUserValidator;
        this.imageValidator = imageValidator;
        this.userMapper = userMapper;
        this.studioActivityPreferencesMapper = studioActivityPreferencesMapper;
        this.preferencesRepository = preferencesRepository;
        this.attributesRepository = attributesRepository;
        this.webClient = builder.baseUrl(DATASCIENCE_API).build();
    }

    @Override
    public StudioActivityDto findById(Long id) {
        LOG.trace("Find studio activity by id: {}", id);
        StudioActivity studioActivity = studioActivityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Studio Activity not found."));

        return studioActivityMapper.entityToDto(studioActivity);
    }

    @Transactional
    @Override
    public StudioActivityListDto save(StudioActivityCreateDto studioActivityCreateDto) throws IOException, SecurityException, ValidationException {
        LOG.trace("Create studio activity: {}", studioActivityCreateDto);
        Long studioId = studioActivityCreateDto.studioId();
        Studio studio = this.studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio with id %d not found.".formatted(studioId)));
        applicationUserValidator.validateAuthorization(userAuthentication.getEmail(), studio.getStudioAdmin().getEmail());
        imageValidator.validateUserProfileImageForUpload(studioActivityCreateDto.profileImageFile(), "Error uploading image to activity");
        ActivityType type = ActivityType.valueOf(studioActivityCreateDto.type());

        if (studio.getStudioActivities() == null) {
            studio.setStudioActivities(new ArrayList<>());
        }

        StudioActivity studioActivity = StudioActivity.builder()
                .name(studioActivityCreateDto.name())
                .price(studioActivityCreateDto.price())
                .capacity(studioActivityCreateDto.capacity())
                .duration(studioActivityCreateDto.duration())
                .dateTime(studioActivityCreateDto.dateTime())
                .type(type)
                .description(studioActivityCreateDto.description())
                .build();

        StudioActivity savedStudioActivity = studioActivityRepository.save(studioActivity);

        ProfileImage profileImage = ProfileImage.builder()
                .name(studioActivityCreateDto.profileImageFile().getOriginalFilename())
                .path(this.imageService.saveImage(studioActivityCreateDto.profileImageFile(), PROFILE_IMAGES_DIR + savedStudioActivity.getStudioActivityId()))
                .build();

        savedStudioActivity.setProfileImage(profileImage);


        studio.getStudioActivities().add(studioActivity);

        StudioActivityPreferences preferences = getStudioActivityPreferences(studioActivityCreateDto, savedStudioActivity, type);

        preferencesRepository.save(preferences);
        studioRepository.save(studio);
        studioActivityRepository.save(savedStudioActivity);
        profileImageRepository.save(profileImage);
        String responseFromPython = webClient.get()
                .uri("/on-preference")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        LOG.info("Response from Python service: {}", responseFromPython);
        return studioActivityMapper.studioActivityEntityToListDto(savedStudioActivity);
    }

    private StudioActivityPreferences getStudioActivityPreferences(StudioActivityCreateDto studioActivityCreateDto, StudioActivity savedStudioActivity,
                                                                   ActivityType type) {
        StudioActivityPreferences preferences = savedStudioActivity.getPreferences();
        if (preferences == null) {
            ActivityTypeAttributes attributes = this.attributesRepository.findByActivityType(type).getFirst();
            preferences = attributes.getAttributes();
        }
        SkillLevel skillLevel = studioActivityCreateDto.skillLevel();

        switch (skillLevel) {
            case BEGINNER -> preferences.setBeginner(true);
            case INTERMEDIATE -> preferences.setIntermediate(true);
            case ADVANCED -> preferences.setAdvanced(true);
            default -> throw new IllegalArgumentException("Unexpected SkillLevel: " + skillLevel);
        }

        int baseDemand = preferences.getPhysicalDemandLevel();
        if (skillLevel == SkillLevel.ADVANCED) {
            baseDemand += 2;
        } else if (skillLevel == SkillLevel.INTERMEDIATE) {
            baseDemand += 1;
        }
        baseDemand += Math.min((int) savedStudioActivity.getDuration() / 30, 2);
        baseDemand = Math.min(baseDemand, 10);
        preferences.setPhysicalDemandLevel(baseDemand);

        if (savedStudioActivity.getCapacity() > 10) {
            preferences.setTeam(true);
            preferences.setIndividual(false);
        } else {
            preferences.setIndividual(true);
            preferences.setTeam(false);
        }

        return preferences;
    }

    @Transactional
    @Override
    public StudioActivityPreferencesDto createStudioActivityPreferences(StudioActivityPreferencesDto studioActivityPreferencesDto) {
        LOG.trace("Create studio activity preferences: {}", studioActivityPreferencesDto);

        Long studioId = studioActivityPreferencesDto.getId();
        Studio studio = this.studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio with id %d not found.".formatted(studioId)));

        applicationUserValidator.validateAuthorization(userAuthentication.getEmail(), studio.getStudioAdmin().getEmail());

        StudioActivityPreferences preferences = StudioActivityPreferences.builder()
                .isIndividual(studioActivityPreferencesDto.isIndividual())
                .isTeam(studioActivityPreferencesDto.isTeam())
                .isWaterBased(studioActivityPreferencesDto.isWaterBased())
                .isIndoor(studioActivityPreferencesDto.isIndoor())
                .isOutdoor(studioActivityPreferencesDto.isOutdoor())
                .isBothIndoorAndOutdoor(studioActivityPreferencesDto.isBothIndoorAndOutdoor())
                .suitableWarmClimate(studioActivityPreferencesDto.isSuitableWarmClimate())
                .suitableColdClimate(studioActivityPreferencesDto.isSuitableColdClimate())
                .rainCompatibility(studioActivityPreferencesDto.isRainCompatibility())
                .windSuitability(studioActivityPreferencesDto.isWindSuitability())
                .involvesUpperBody(studioActivityPreferencesDto.isFocusUpperBody())
                .involvesLowerBody(studioActivityPreferencesDto.isFocusLowerBody())
                .involvesCore(studioActivityPreferencesDto.isFocusCore())
                .involvesFullBody(studioActivityPreferencesDto.isFocusFullBody())
                .isBeginner(studioActivityPreferencesDto.isBeginner())
                .isIntermediate(studioActivityPreferencesDto.isIntermediate())
                .isAdvanced(studioActivityPreferencesDto.isAdvanced())
                .physicalDemandLevel(studioActivityPreferencesDto.getPhysicalDemandLevel())
                .goalStrength(studioActivityPreferencesDto.isGoalStrength())
                .goalEndurance(studioActivityPreferencesDto.isGoalEndurance())
                .goalFlexibility(studioActivityPreferencesDto.isGoalFlexibility())
                .goalBalanceCoordination(studioActivityPreferencesDto.isGoalBalanceCoordination())
                .goalMentalFocus(studioActivityPreferencesDto.isGoalMentalFocus())
                .build();

        StudioActivityPreferences savedPreferences = studioActivityPreferencesRepository.save(preferences);

        return studioActivityPreferencesMapper.entityToDto(savedPreferences);
    }


    @Override
    public StudioActivityPreferencesDto updateStudioActivityPreferences(StudioActivityPreferencesDto studioActivityPreferencesDto) {
        LOG.trace("Update studio activity preferences: {}", studioActivityPreferencesDto);

        Long studioId = studioActivityPreferencesDto.getId();
        Studio studio = this.studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio with id %d not found.".formatted(studioId)));

        applicationUserValidator.validateAuthorization(userAuthentication.getEmail(), studio.getStudioAdmin().getEmail());

        studioActivityPreferencesRepository.updateActivityPreferences(
                studioActivityPreferencesDto.getId(),
                studioActivityPreferencesDto.isIndividual(),
                studioActivityPreferencesDto.isTeam(),
                studioActivityPreferencesDto.isWaterBased(),
                studioActivityPreferencesDto.isIndoor(),
                studioActivityPreferencesDto.isOutdoor(),
                studioActivityPreferencesDto.isBothIndoorAndOutdoor(),
                studioActivityPreferencesDto.isSuitableWarmClimate(),
                studioActivityPreferencesDto.isSuitableColdClimate(),
                studioActivityPreferencesDto.isRainCompatibility(),
                studioActivityPreferencesDto.isWindSuitability(),
                studioActivityPreferencesDto.isFocusUpperBody(),
                studioActivityPreferencesDto.isFocusLowerBody(),
                studioActivityPreferencesDto.isFocusCore(),
                studioActivityPreferencesDto.isFocusFullBody(),
                studioActivityPreferencesDto.isBeginner(),
                studioActivityPreferencesDto.isIntermediate(),
                studioActivityPreferencesDto.isAdvanced(),
                studioActivityPreferencesDto.getPhysicalDemandLevel(),
                studioActivityPreferencesDto.isGoalStrength(),
                studioActivityPreferencesDto.isGoalEndurance(),
                studioActivityPreferencesDto.isGoalFlexibility(),
                studioActivityPreferencesDto.isGoalBalanceCoordination(),
                studioActivityPreferencesDto.isGoalMentalFocus()
        );

        StudioActivityPreferences updatedPreferences = studioActivityPreferencesRepository.findById(Math.toIntExact(studioActivityPreferencesDto.getId()))
                .orElseThrow(() -> new NotFoundException("Updated preferences not found."));

        return studioActivityPreferencesMapper.entityToDto(updatedPreferences);
    }


    @Override
    public StudioActivityDto update(StudioActivityDto studioActivityDto) throws NotFoundException, SecurityException {
        LOG.trace("Update studio activity: {}", studioActivityDto);

        StudioActivity newStudioActivity = studioActivityRepository.findById(studioActivityDto.studioActivityId())
                .orElseThrow(() -> new NotFoundException("Studio Activity not found."));

        Studio studio = studioRepository.findByStudioActivityId(studioActivityDto.studioActivityId());
        applicationUserValidator.validateAuthorization(userAuthentication.getEmail(), studio.getStudioAdmin().getEmail());
        newStudioActivity.setName(studioActivityDto.name());
        newStudioActivity.setDescription(studioActivityDto.description());
        newStudioActivity.setPrice(studioActivityDto.price());
        newStudioActivity.setDuration(studioActivityDto.duration());
        newStudioActivity.setDateTime(studioActivityDto.dateTime());
        newStudioActivity.setType(studioActivityDto.type());

        return studioActivityMapper.entityToDto(studioActivityRepository.save(newStudioActivity));
    }

    @Transactional
    @Override
    public void delete(Long id) throws NotFoundException, SecurityException {
        LOG.trace("Delete studio activity with id: {}", id);
        StudioActivity activity = studioActivityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Studio Activity to delete not found."));
        Studio studio = studioRepository.findByStudioActivityId(id);
        applicationUserValidator.validateAuthorization(userAuthentication.getEmail(), studio.getStudioAdmin().getEmail());
        studioActivityRepository.deleteStudioActivityByStudioActivityId(activity.getStudioActivityId());
    }

    @Override
    public StudioForActivityDto getStudioForActivity(Long activityId) {
        LOG.trace("Get studio for activity with id: {}", activityId);
        StudioActivityDto activity = findById(activityId);
        Studio studio = studioRepository.findStudioByStudioActivity(studioActivityMapper.dtoToEntity(activity));
        return studioMapper.entityToStudioDtoForActivity(studio);
    }

    @Transactional
    @Override
    public StudioActivityDto bookAnActivity(StudioActivityDto studioActivityDto) throws ConflictException {
        LOG.trace("bookingActivity({})", studioActivityDto);
        StudioActivity activity = studioActivityMapper.dtoToEntity(findById(studioActivityDto.studioActivityId()));
        ApplicationUser user = applicationUserRepository.findByEmail(userAuthentication.getEmail());
        List<StudioActivity> userActivities = user.getStudioActivities();
        if (bookingExist(user, activity)) {
            throw new ConflictException("Studio Activity already booked", new ArrayList<>());
        }
        userActivities.add(activity);
        user.setStudioActivities(userActivities);
        applicationUserRepository.save(user);
        String responseFromPython = webClient.get()
                .uri("/collaborative-filtering/{user_id}", user.getApplicationUserId())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        LOG.info("Response from Python service: {}", responseFromPython);
        return studioActivityMapper.entityToDto(activity);
    }

    @Transactional
    @Override
    public void unbookAnActivity(StudioActivityDto studioActivityDto) throws ConflictException {
        LOG.trace("Unbook studio activity with id: {}", studioActivityDto);
        if ((LocalDateTime.now().plusDays(1)).isAfter(studioActivityDto.dateTime())) {
            throw new ConflictException("Bookings can only be canceled up to 24 hours before the scheduled time.", List.of("Cancellation deadline exceeded"));
        }


        StudioActivity activity = studioActivityMapper.dtoToEntity(findById(studioActivityDto.studioActivityId()));
        ApplicationUser user = applicationUserRepository.findByEmail(userAuthentication.getEmail());
        List<StudioActivity> userActivities = user.getStudioActivities();
        userActivities.removeIf(studioActivity -> Objects.equals(studioActivity.getStudioActivityId(), activity.getStudioActivityId()));
    }

    @Override
    public List<StudioActivityListDto> getActivities(Long studioId) throws NotFoundException {
        LOG.trace("getActivities({})", studioId);
        Studio studio = this.studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio with id %d not found.".formatted(studioId)));
        List<StudioActivity> activities = studioActivityRepository.findCurrentSortedStudioActivities(studio.getStudioId());
        return activities.stream()
                .map(studioActivityMapper::studioActivityEntityToListDto)
                .toList();
    }

    @Override
    public List<String> getActivityTypes() {
        LOG.trace("getActivityTypes()");
        return Arrays.stream(ActivityType.values())
                .map(ActivityType::name)
                .toList();
    }

    @Override
    public List<UserInfoDto> getParticipatingFriends(Long activityId) {
        StudioActivityDto activityDto = findById(activityId);
        StudioActivity activity = studioActivityMapper.dtoToEntity(activityDto);
        List<ApplicationUser> participants = activity.getApplicationUsers();
        ApplicationUser currentUser = applicationUserRepository.findByEmail(userAuthentication.getEmail());

        Set<ApplicationUser> currentUserFriends = currentUser.getFriends();
        List<ApplicationUser> filteredFriends = currentUserFriends.stream()
                .filter(friend -> participants.stream()
                        .anyMatch(participant -> participant.getEmail().equals(friend.getEmail())))
                .toList();

        return userMapper.usersToUserInfoDtos(filteredFriends);
    }

    @Override
    public boolean alreadyBooked(Long activityId) {
        ApplicationUser currentUser = applicationUserRepository.findByEmail(userAuthentication.getEmail());
        StudioActivity activity = studioActivityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Studio Activity not found."));
        return currentUser.getStudioActivities().contains(activity);
    }

    @Override
    public Page<StudioActivityTypeSearchResponseDto> getActivitiesByType(SearchActivitiesDto type) {
        LOG.trace("getActivitiesByType: {}", type);
        Pageable pageable = PageRequest.of(type.pageIndex(), type.pageSize());
        ActivityType activityType = ActivityType.valueOf(type.activityType());
        Page<StudioActivity> activities = studioActivityRepository.findStudioActivitiesByType(activityType, pageable);
        List<StudioActivityTypeSearchResponseDto> activitiesDto = activities.stream()
                .map(studioActivityMapper::studioActivityEntityToTyeSearchResponseDto).toList();
        return new PageImpl<>(activitiesDto, pageable, activities.getTotalElements());
    }

    private boolean bookingExist(ApplicationUser user, StudioActivity activity) {
        LOG.trace("bookingExist({},{})", user, activity);
        for (StudioActivity a : user.getStudioActivities()) {
            if (a.getStudioActivityId().equals(activity.getStudioActivityId())) {
                return true;
            }
        }
        return false;
    }


}
