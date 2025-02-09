package com.inso.sila.service.impl;

import com.inso.sila.endpoint.dto.studio.studio.CreatedStudioDto;
import com.inso.sila.endpoint.dto.studio.faqs.FaqsDto;
import com.inso.sila.endpoint.dto.studio.instructor.InstructorCreateDto;
import com.inso.sila.endpoint.dto.studio.instructor.InstructorDto;
import com.inso.sila.endpoint.dto.studio.membership.MembershipDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioApprovalDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioCreateDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioInfoDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioSearchDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioUpdateDto;
import com.inso.sila.endpoint.mapper.FaqsMapper;
import com.inso.sila.endpoint.mapper.InstructorMapper;
import com.inso.sila.endpoint.mapper.MembershipMapper;
import com.inso.sila.endpoint.mapper.StudioMapper;
import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.Faqs;
import com.inso.sila.entity.GalleryImage;
import com.inso.sila.entity.Instructor;
import com.inso.sila.entity.Membership;
import com.inso.sila.entity.ProfileImage;
import com.inso.sila.entity.Studio;
import com.inso.sila.exception.ConflictException;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.exception.ValidationException;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.FaqsRepository;
import com.inso.sila.repository.InstructorRepository;
import com.inso.sila.repository.GalleryImageRepository;
import com.inso.sila.repository.MembershipRepository;
import com.inso.sila.repository.ProfileImageRepository;
import com.inso.sila.repository.StudioRepository;
import com.inso.sila.security.UserAuthentication;
import com.inso.sila.service.ImageService;
import com.inso.sila.service.MailService;
import com.inso.sila.service.StudioService;
import com.inso.sila.service.validation.ApplicationUserValidator;
import com.inso.sila.service.validation.ImageValidator;
import com.inso.sila.service.validation.StudioValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudioServiceImpl implements StudioService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String PROFILE_IMAGES_DIR = "assets/studio/profile-images/";
    private static final String GALLERY_IMAGES_DIR = "assets/studio/gallery-images/";
    private static final String INSTRUCTOR_PROFILE_IMAGES_DIR = "assets/instructor-profile-images/";
    private final StudioRepository studioRepository;
    private final FaqsRepository faqsRepository;
    private final FaqsMapper faqsMapper;
    private final MembershipRepository membershipRepository;
    private final StudioMapper studioMapper;
    private final MembershipMapper membershipMapper;
    private final ImageService imageService;
    private final ProfileImageRepository profileImageRepository;
    private final GalleryImageRepository galleryImageRepository;
    private final UserAuthentication userAuthentication;
    private final ApplicationUserRepository applicationUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationUserValidator applicationUserValidator;
    private final MailService mailService;
    private final InstructorRepository instructorRepository;
    private final InstructorMapper instructorMapper;
    private final ImageValidator imageValidator;
    private final StudioValidator studioValidator;
    private final WebClient webClient;
    private static final String DATASCIENCE_API = "http://datascience:5000";

    public StudioServiceImpl(StudioRepository studioRepository, FaqsRepository faqsRepository,
                             FaqsMapper faqsMapper, MembershipRepository membershipRepository,
                             StudioMapper studioMapper, MembershipMapper membershipMapper,
                             ImageService imageService, ProfileImageRepository profileImageRepository,
                             GalleryImageRepository galleryImageRepository, UserAuthentication userAuthentication,
                             ApplicationUserRepository applicationUserRepository, PasswordEncoder passwordEncoder,
                             ApplicationUserValidator applicationUserValidator, MailService mailService,
                             InstructorRepository instructorRepository, InstructorMapper instructorMapper,
                             ImageValidator imageValidator, StudioValidator studioValidator, WebClient.Builder builder) {
        this.studioRepository = studioRepository;
        this.faqsRepository = faqsRepository;
        this.faqsMapper = faqsMapper;
        this.membershipRepository = membershipRepository;
        this.studioMapper = studioMapper;
        this.membershipMapper = membershipMapper;
        this.imageService = imageService;
        this.profileImageRepository = profileImageRepository;
        this.galleryImageRepository = galleryImageRepository;
        this.userAuthentication = userAuthentication;
        this.applicationUserRepository = applicationUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationUserValidator = applicationUserValidator;
        this.mailService = mailService;
        this.instructorRepository = instructorRepository;
        this.instructorMapper = instructorMapper;
        this.imageValidator = imageValidator;
        this.studioValidator = studioValidator;
        this.webClient = builder.baseUrl(DATASCIENCE_API).build();
    }


    @Override
    public CreatedStudioDto addStudio(StudioCreateDto studioCreateDto) throws IOException, ValidationException, ConflictException {
        LOG.trace("addStudio({})", studioCreateDto);
        applicationUserValidator.validatePasswordSetting(studioCreateDto.password(), studioCreateDto.confirmPassword());
        studioValidator.validateUniqueStudioLocation(studioCreateDto.location());
        applicationUserValidator.checkIfUserExists(studioCreateDto.email());
        imageValidator.validateUserProfileImageForUpload(studioCreateDto.profileImageFile(), "Image size not valid");
        ApplicationUser studioAdmin = ApplicationUser.builder()
                .firstName("Studio Admin Of")
                .lastName(studioCreateDto.name())
                .email(studioCreateDto.email())
                .password(passwordEncoder.encode(studioCreateDto.password()))
                .isStudioAdmin(true)
                .isAdmin(false)
                .isLocked(false)
                .loginAttempts(0)
                .location(studioCreateDto.location())
                .longitude(studioCreateDto.longitude())
                .latitude(studioCreateDto.latitude())
                .build();

        applicationUserRepository.save(studioAdmin);
        Studio studio = Studio.builder()
                .name(studioCreateDto.name())
                .description(studioCreateDto.description())
                .location(studioCreateDto.location())
                .longitude(studioCreateDto.longitude())
                .latitude(studioCreateDto.latitude())
                .approved(false)
                .studioActivities(new ArrayList<>())
                .memberships(new ArrayList<>())
                .faqs(new ArrayList<>())
                .likedFromApplicationUsers(new ArrayList<>())
                .studioAdmin(studioAdmin)
                .build();

        Studio savedStudio = studioRepository.save(studio);

        ProfileImage profileImage = ProfileImage.builder()
                .name(studioCreateDto.profileImageFile().getOriginalFilename())
                .path(imageService.saveImage(studioCreateDto.profileImageFile(), PROFILE_IMAGES_DIR + savedStudio.getStudioId()))
                .build();

        savedStudio.setProfileImage(profileImage);

        profileImageRepository.save(profileImage);
        return studioMapper.entityToCreatedStudioDto(studioRepository.save(savedStudio));
    }

    @Override
    public StudioInfoDto updateApprovalOfStudio(StudioApprovalDto studio) {
        LOG.trace("updateApprovalOfStudio({})", studio);
        studioRepository.updateApprovalOfStudio(studio.studioId(), studio.approved());
        Studio updatedStudio = this.studioRepository.findById(studio.studioId())
                .orElseThrow(() -> new NotFoundException("Studio to approve not found"));
        mailService.sendApprovalEmailToStudioAdmin(updatedStudio.getStudioAdmin().getEmail(), updatedStudio.getName());
        return studioMapper.entityToInfoDto(updatedStudio);
    }

    @Override
    public StudioDto updateStudio(StudioUpdateDto studioUpdateDto, Long studioId) throws NotFoundException, SecurityException {
        LOG.trace("updateStudio({})", studioUpdateDto);
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio to update not found"));
        applicationUserValidator.validateAuthorization(userAuthentication.getEmail(), studio.getStudioAdmin().getEmail());
        studioRepository.updateStudio(studio.getStudioId(), studioUpdateDto.name(), studioUpdateDto.description(),
                studioUpdateDto.location(), studioUpdateDto.longitude(), studioUpdateDto.latitude());
        ApplicationUser applicationUser = applicationUserRepository.findByEmail(userAuthentication.getEmail());
        return studioMapper.entityToDto(studioRepository.findByStudioId(studioId), applicationUser);
    }

    @Override
    public StudioDto updateProfileImage(MultipartFile profileImageFile, Long studioId) throws IOException, ValidationException, NotFoundException, SecurityException {
        LOG.trace("updateProfileImage({})", studioId);
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio to upload profile image for not found"));
        applicationUserValidator.validateAuthorization(userAuthentication.getEmail(), studio.getStudioAdmin().getEmail());
        imageValidator.validateUserProfileImageForUpload(profileImageFile, "Validation error uploading profile image");
        String profileImagePath = imageService.saveImage(profileImageFile, PROFILE_IMAGES_DIR + studio.getStudioId());
        if (studio.getProfileImage() == null) {
            ProfileImage profileImage = ProfileImage.builder()
                    .name(profileImageFile.getOriginalFilename())
                    .path(profileImagePath)
                    .studio(studio).build();
            profileImageRepository.save(profileImage);
            studio.setProfileImage(profileImage);
            studioRepository.save(studio);
        } else {
            ProfileImage profileImage = profileImageRepository.findById(studio.getProfileImage().getProfileImageId())
                    .orElseThrow(() -> new NotFoundException("Can't locate profile image for studio " + studio.getName()));
            String oldFilePath = profileImage.getPath();
            imageService.deleteOldFile(oldFilePath);
            profileImageRepository.updateProfileImage(profileImage.getProfileImageId(), profileImagePath, profileImageFile.getOriginalFilename());
        }
        return studioMapper.entityToDto(studioRepository.findByStudioId(studioId));
    }


    @Override
    public List<StudioDto> getAllStudios() {
        LOG.trace("getAllStudios()");
        return studioMapper.entityToDto(studioRepository.findByApprovedTrue());
    }

    @Override
    public List<StudioDto> getAllLikedStudiosForUser() {
        LOG.trace("getLikedStudiosForUser()");

        ApplicationUser applicationUser = applicationUserRepository.findByEmail(userAuthentication.getEmail());

        List<Studio> favouriteStudios = applicationUser.getFavouriteStudios();
        return studioMapper.entityToDto(favouriteStudios);
    }

    @Override
    public List<StudioInfoDto> getNotApprovedStudios() {
        LOG.trace("getNotApprovedStudios()");
        List<Studio> studios = studioRepository.findByApprovedFalse();
        return studios.stream()
                .map(studioMapper::entityToInfoDto)
                .toList();
    }

    @Override
    public StudioDto deleteStudio(Long studioId) throws NotFoundException {
        LOG.trace("deleteStudio({})", studioId);
        Studio studio = studioRepository.findByIdWithRelations(studioId)
                .orElseThrow(() -> new NotFoundException("Studio with ID " + studioId + " not found"));

        studioRepository.delete(studio);

        ApplicationUser applicationUser = applicationUserRepository.findByEmail(userAuthentication.getEmail());
        return studioMapper.entityToDto(studio, applicationUser);
    }

    @Override
    public StudioDto getStudioById(Long studioId) throws NotFoundException {
        LOG.trace("getStudioById({})", studioId);
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio with ID " + studioId + " not found"));

        ApplicationUser applicationUser = applicationUserRepository.findByEmail(userAuthentication.getEmail());

        return studioMapper.entityToDto(studio, applicationUser);
    }

    @Override
    public FaqsDto addFaq(FaqsDto faqs, Long studioId) throws SecurityException, NotFoundException {
        LOG.trace("addFaq({}, {})", faqs, studioId);
        Faqs faq = Faqs.builder()
                .question(faqs.question())
                .answer(faqs.answer())
                .build();

        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio not found with id " + studioId));
        applicationUserValidator.validateAuthorization(userAuthentication.getEmail(), studio.getStudioAdmin().getEmail());
        faq.setStudio(studio);
        studio.getFaqs().add(faq);

        return faqsMapper.faqsToFaqsDto(faqsRepository.save(faq));
    }

    @Override
    public MembershipDto addMembership(MembershipDto membershipDto, Long studioId) throws NotFoundException, SecurityException {
        LOG.trace("addMembership({}, {})", membershipDto, studioId);
        Membership membership = Membership.builder()
                .name(membershipDto.name())
                .duration(membershipDto.duration())
                .minDuration(membershipDto.minDuration())
                .price(membershipDto.price())
                .build();

        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio not found with id " + studioId));
        applicationUserValidator.validateAuthorization(userAuthentication.getEmail(), studio.getStudioAdmin().getEmail());
        membership.setStudio(studio);
        studio.getMemberships().add(membership);
        return membershipMapper.entityToDto(membershipRepository.save(membership));

    }

    @Override
    public StudioDto addFavouriteStudio(Long studioId) throws NotFoundException {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio with id %d not found.".formatted(studioId)));

        ApplicationUser applicationUser = applicationUserRepository.findByEmail(userAuthentication.getEmail());

        applicationUser.getFavouriteStudios().add(studio);
        applicationUserRepository.save(applicationUser);

        studio.getLikedFromApplicationUsers().add(applicationUser);
        Studio savedStudio = studioRepository.save(studio);
        String responseFromPython = webClient.get()
                .uri("/collaborative-filtering/{user_id}", applicationUser.getApplicationUserId())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        LOG.info("Response from Python service: {}", responseFromPython);
        return studioMapper.entityToDto(savedStudio, applicationUser);

    }

    @Override
    public StudioDto removeFavouriteStudio(Long studioId) throws NotFoundException {

        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio with id %d not found.".formatted(studioId)));

        ApplicationUser applicationUser = applicationUserRepository.findByEmail(userAuthentication.getEmail());

        applicationUser.getFavouriteStudios().remove(studio);
        applicationUserRepository.save(applicationUser);
        studio.getLikedFromApplicationUsers().remove(applicationUser);
        return studioMapper.entityToDto(studioRepository.save(studio), applicationUser);
    }

    @Override
    public Page<StudioInfoDto> searchByNameAndLocation(StudioSearchDto studioSearchDto) {
        LOG.trace("searchByNameAndLocation({}, {})", studioSearchDto.name(), studioSearchDto.location());
        Pageable pageable = PageRequest.of(studioSearchDto.pageIndex(), studioSearchDto.pageSize());
        Page<Studio> studios = studioRepository.findByNameAndLocation(studioSearchDto.name(), studioSearchDto.location(), pageable);
        List<StudioInfoDto> studiosDto = studios.stream()
                .map(studioMapper::entityToInfoDto)
                .toList();
        return new PageImpl<>(studiosDto, pageable, studios.getTotalElements());
    }

    @Override
    public Long getStudioIdByAdmin(String email) {
        return studioRepository.findByStudioAdminEmail(email).getStudioId();
    }

    @Override
    public Boolean isCurrentUserAdmin(Long studioId) throws NotFoundException {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio with id %d not found.".formatted(studioId)));
        String currentUser = userAuthentication.getEmail();
        if (studio.getStudioAdmin() == null) {
            return false;
        }
        return studio.getStudioAdmin().getEmail().equals(currentUser);
    }

    @Override
    public InstructorDto addInstructor(InstructorCreateDto instructorDto, Long studioId) throws IOException, NotFoundException, ValidationException {
        LOG.trace("addInstructor({}, {})", instructorDto, studioId);
        imageValidator.validateUserProfileImageForUpload(instructorDto.profileImage(), "Image couldn't be uploaded");

        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio not found with id " + studioId));
        applicationUserValidator.validateAuthorization(userAuthentication.getEmail(), studio.getStudioAdmin().getEmail());
        Instructor instructor = Instructor.builder()
                .firstName(instructorDto.firstName())
                .lastName(instructorDto.lastName())
                .build();
        studio.getInstructors().add(instructor);
        Instructor savedInstructor = instructorRepository.save(instructor);
        studioRepository.save(studio);

        ProfileImage profileImage = ProfileImage.builder()
                .name(instructorDto.profileImage().getOriginalFilename())
                .path(imageService.saveImage(instructorDto.profileImage(), INSTRUCTOR_PROFILE_IMAGES_DIR + savedInstructor.getInstructorId()))
                .build();

        savedInstructor.setProfileImage(profileImage);
        return instructorMapper.entityToDto(instructorRepository.save(savedInstructor));
    }

    @Override
    public StudioDto addGalleryImages(Long studioId, MultipartFile[] files) throws IOException, NotFoundException, SecurityException, ValidationException {
        for (MultipartFile file : files) {
            imageValidator.validateUserProfileImageForUpload(file, "Validation error Uploading file");
        }
        Studio studio = this.studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio with id %d not found.".formatted(studioId)));
        applicationUserValidator.validateAuthorization(userAuthentication.getEmail(), studio.getStudioAdmin().getEmail());
        List<GalleryImage> galleryImages = new ArrayList<>();

        for (MultipartFile file : files) {
            galleryImages.add(
                    GalleryImage.builder()
                            .name(file.getOriginalFilename())
                            .path(imageService.saveImage(file, GALLERY_IMAGES_DIR + studioId))
                            .build()
            );

        }

        studio.getGalleryImages().addAll(galleryImages);
        galleryImageRepository.saveAll(galleryImages);
        return studioMapper.entityToDto(studioRepository.save(studio));
    }

}
