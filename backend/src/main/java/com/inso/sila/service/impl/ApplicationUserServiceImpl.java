package com.inso.sila.service.impl;

import com.inso.sila.endpoint.dto.studio.studio.StudioDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityDto;
import com.inso.sila.endpoint.dto.user.UpdateUserInfoDto;
import com.inso.sila.endpoint.dto.user.UserDetailDto;
import com.inso.sila.endpoint.dto.user.UserEmailDto;
import com.inso.sila.endpoint.dto.user.UserInfoDto;
import com.inso.sila.endpoint.dto.user.UserLoginDto;
import com.inso.sila.endpoint.dto.user.UserPreferencesDto;
import com.inso.sila.endpoint.dto.user.UserRegisterDto;
import com.inso.sila.endpoint.dto.user.UserSearchDto;
import com.inso.sila.endpoint.dto.user.UserUpdatePasswordDto;
import com.inso.sila.endpoint.mapper.StudioActivityMapper;
import com.inso.sila.endpoint.mapper.StudioMapper;
import com.inso.sila.endpoint.mapper.UserMapper;
import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.ApplicationUserPreferences;
import com.inso.sila.entity.Membership;
import com.inso.sila.entity.ProfileImage;
import com.inso.sila.entity.Studio;
import com.inso.sila.entity.StudioActivity;
import com.inso.sila.exception.ConflictException;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.exception.ValidationException;
import com.inso.sila.repository.ApplicationUserPreferencesRepository;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.MembershipRepository;
import com.inso.sila.repository.ProfileImageRepository;
import com.inso.sila.repository.StudioActivityRepository;
import com.inso.sila.repository.StudioRepository;
import com.inso.sila.security.JwtTokenizer;
import com.inso.sila.security.UserAuthentication;
import com.inso.sila.service.ApplicationUserService;
import com.inso.sila.service.ImageService;
import com.inso.sila.service.MailService;
import com.inso.sila.service.validation.ApplicationUserValidator;
import com.inso.sila.service.validation.ImageValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationUserServiceImpl implements ApplicationUserService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final JwtTokenizer jwtTokenizer;
    private final ApplicationUserRepository applicationUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationUserValidator userValidator;
    private final MailService mailService;
    private final ApplicationUserValidator applicationUserValidator;
    private final UserAuthentication userAuthentication;
    private final UserMapper userMapper;
    private final ImageService imageService;
    private final ProfileImageRepository profileImageRepository;
    private final StudioRepository studioRepository;
    private final StudioActivityMapper studioActivityMapper;
    private final ApplicationUserPreferencesRepository applicationUserPreferencesRepository;
    private final MembershipRepository membershipRepository;

    private static final String PROFILE_IMAGES_DIR = "assets/user/profile-images/";
    private final ImageValidator imageValidator;
    private final StudioActivityRepository studioActivityRepository;
    private final StudioMapper studioMapper;


    @Override
    public String login(UserLoginDto userLoginDto) throws BadCredentialsException, LockedException {
        LOG.trace("Login User({})", userLoginDto);
        try {
            UserDetails userDetails = loadUserByUsername(userLoginDto.email());
            if (userDetails != null) {
                if (!userDetails.isAccountNonLocked()) {
                    throw new LockedException("User account is locked");
                }
                if (userDetails.isAccountNonExpired()
                        && userDetails.isCredentialsNonExpired()
                        && passwordEncoder.matches(userLoginDto.password(), userDetails.getPassword())) {
                    List<String> roles = userDetails.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList();
                    applicationUserRepository.setLoginAttempts(userLoginDto.email(), 0);
                    return jwtTokenizer.getAuthToken(userDetails.getUsername(), roles);
                }
            }

            throw new BadCredentialsException("Username or password is incorrect");
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("Username or password is incorrect");
        }
    }

    @Override
    public UserEmailDto registerUser(UserRegisterDto userRegisterDto) throws ConflictException, ValidationException {
        LOG.debug("Create new user {}", userRegisterDto);
        applicationUserValidator.validatePasswordSetting(userRegisterDto.password(), userRegisterDto.passwordConfirmation());
        applicationUserValidator.checkIfUserExists(userRegisterDto.email());
        ApplicationUser newApplicationUser = ApplicationUser.builder()
                .isAdmin(Boolean.FALSE)
                .isStudioAdmin(Boolean.FALSE)
                .firstName(userRegisterDto.firstName())
                .lastName(userRegisterDto.lastName())
                .gender(userRegisterDto.gender())
                .email(userRegisterDto.email())
                .location(userRegisterDto.location())
                .longitude(userRegisterDto.longitude())
                .latitude(userRegisterDto.latitude())
                .password(passwordEncoder.encode(userRegisterDto.password()))
                .isLocked(Boolean.FALSE)
                .preferencesSet(Boolean.FALSE)
                .loginAttempts(0)
                .build();
        applicationUserRepository.save(newApplicationUser);

        ApplicationUser savedUser = applicationUserRepository.findByEmail(userRegisterDto.email());
        return new UserEmailDto(savedUser.getEmail());
    }

    @Transactional
    @Override
    public UserInfoDto createUserPreferences(UserPreferencesDto userPreferencesDto) throws ConflictException {
        LOG.trace("createUserPreferences: {}", userPreferencesDto);

        String currentUserEmail = userAuthentication.getEmail();
        userAuthentication.checkBlockedStatus(currentUserEmail);

        ApplicationUser user = applicationUserRepository.findByEmail(currentUserEmail);
        if (user.getPreferences() != null) {
            throw new ConflictException("Preferences already exist for user with ID: ", List.of(user.getApplicationUserId().toString()));
        }

        ApplicationUserPreferences preferences = ApplicationUserPreferences.builder()
                .user(user)
                .prefersIndividual(userPreferencesDto.prefersIndividual())
                .prefersTeam(userPreferencesDto.prefersTeam())
                .prefersWaterBased(userPreferencesDto.prefersWaterBased())
                .prefersOutdoor(userPreferencesDto.prefersOutdoor())
                .prefersIndoor(userPreferencesDto.prefersIndoor())
                .prefersBothIndoorAndOutdoor(userPreferencesDto.prefersBothIndoorAndOutdoor())
                .prefersWarmClimate(userPreferencesDto.prefersWarmClimate())
                .prefersColdClimate(userPreferencesDto.prefersColdClimate())
                .rainCompatibility(userPreferencesDto.rainCompatibility())
                .windSuitability(userPreferencesDto.windSuitability())
                .focusUpperBody(userPreferencesDto.focusUpperBody())
                .focusLowerBody(userPreferencesDto.focusLowerBody())
                .focusCore(userPreferencesDto.focusCore())
                .focusFullBody(userPreferencesDto.focusFullBody())
                .isBeginner(userPreferencesDto.isBeginner())
                .isIntermediate(userPreferencesDto.isIntermediate())
                .isAdvanced(userPreferencesDto.isAdvanced())
                .physicalDemandLevel(userPreferencesDto.physicalDemandLevel())
                .goalStrength(userPreferencesDto.goalStrength())
                .goalEndurance(userPreferencesDto.goalEndurance())
                .goalFlexibility(userPreferencesDto.goalFlexibility())
                .goalBalanceCoordination(userPreferencesDto.goalBalanceCoordination())
                .goalMentalFocus(userPreferencesDto.goalMentalFocus())
                .build();

        user.setPreferences(preferences);
        user.setPreferencesSet(true);

        applicationUserPreferencesRepository.save(preferences);
        applicationUserRepository.save(user);

        return userMapper.userToUserInfoDto(user);
    }

    @Transactional
    @Override
    public UserInfoDto updateUserPreferences(UserPreferencesDto userPreferencesDto) {
        LOG.trace("updateUserPreferences{}", userPreferencesDto);
        String currentUser = userAuthentication.getEmail();
        userAuthentication.checkBlockedStatus(currentUser);

        Long id = applicationUserRepository.findByEmail(currentUser).getApplicationUserId();
        applicationUserPreferencesRepository.updateUserPreferences(
                id,
                userPreferencesDto.prefersIndividual(),
                userPreferencesDto.prefersTeam(),
                userPreferencesDto.prefersWaterBased(),
                userPreferencesDto.prefersIndoor(),
                userPreferencesDto.prefersOutdoor(),
                userPreferencesDto.prefersBothIndoorAndOutdoor(),
                userPreferencesDto.prefersWarmClimate(),
                userPreferencesDto.prefersColdClimate(),
                userPreferencesDto.rainCompatibility(),
                userPreferencesDto.windSuitability(),
                userPreferencesDto.focusUpperBody(),
                userPreferencesDto.focusLowerBody(),
                userPreferencesDto.focusCore(),
                userPreferencesDto.focusFullBody(),
                userPreferencesDto.isBeginner(),
                userPreferencesDto.isIntermediate(),
                userPreferencesDto.isAdvanced(),
                userPreferencesDto.physicalDemandLevel(),
                userPreferencesDto.goalStrength(),
                userPreferencesDto.goalEndurance(),
                userPreferencesDto.goalFlexibility(),
                userPreferencesDto.goalBalanceCoordination(),
                userPreferencesDto.goalMentalFocus()
        );

        var user = applicationUserRepository.findByEmail(currentUser);
        return userMapper.userToUserInfoDto(user);
    }

    @Override
    public boolean checkPreferences() {
        String email = userAuthentication.getEmail();
        userAuthentication.checkBlockedStatus(email);
        LOG.trace("checkPreferences({})", email);
        var user = applicationUserRepository.findByEmail(email);
        return user.isPreferencesSet();
    }

    @Override
    public UserInfoDto getUserInfo(String email) throws NotFoundException, SecurityException {
        LOG.trace("gettingUserInfo({})", email);
        userValidator.validateExistingEmail(email);
        userAuthentication.checkBlockedStatus(email);
        userValidator.validateAuthorization(email, userAuthentication.getEmail());
        var result = applicationUserRepository.findByEmail(email);
        return userMapper.userToUserInfoDto(result);
    }

    @Override
    public UserInfoDto updateUserInfo(UpdateUserInfoDto userInfoDto) throws SecurityException {
        LOG.trace("updateUserInfo{}", userInfoDto);
        String currentUser = userAuthentication.getEmail();
        userAuthentication.checkBlockedStatus(currentUser);
        userValidator.validateAuthorization(userInfoDto.email(), currentUser);

        applicationUserRepository.updateApplicationUser(
                currentUser,
                userInfoDto.firstName(),
                userInfoDto.lastName(),
                userInfoDto.location(),
                userInfoDto.longitude(),
                userInfoDto.latitude(),
                userInfoDto.gender()
        );

        var result = applicationUserRepository.findByEmail(userInfoDto.email());
        return userMapper.userToUserInfoDto(result);
    }

    @Override
    public UserInfoDto deleteUser(String email) throws SecurityException, ConflictException {
        LOG.trace("deleteUser({})", email);
        userValidator.validateExistingEmail(email);
        userValidator.validateAuthorizationForDeleteUser(email, userAuthentication.getEmail());
        if (userAuthentication.getEmail().equals(email)) {
            userAuthentication.checkBlockedStatus(email);
        }
        ApplicationUser result = applicationUserRepository.findByEmail(email);
        if (result.isAdmin()) {
            userValidator.checkForLastAdmin();
        }
        Studio studio = studioRepository.findByStudioAdminEmail(result.getEmail());
        if (studio != null) {
            // remove studio from liked likedStudios of users
            List<ApplicationUser> likedUsers = studio.getLikedFromApplicationUsers();
            if (likedUsers != null) {
                for (ApplicationUser user : likedUsers) {
                    user.getFavouriteStudios().remove(studio);
                }
                applicationUserRepository.saveAll(likedUsers);
            }

            studioRepository.delete(studio); // Cascade will delete the user
        } else {
            // No studio is associated, delete the user directly
            Set<Membership> memberships = result.getMemberships();
            for (Membership membership : memberships) {
                membership.getApplicationUsers().remove(result);
            }

            membershipRepository.saveAll(memberships);

            applicationUserRepository.delete(result);
        }
        return userMapper.userToUserInfoDto(result);
    }

    @Override
    public String uploadUserProfileImage(MultipartFile file) throws IOException, ValidationException {
        LOG.trace("uploadProfileImage({})", file);
        imageValidator.validateUserProfileImageForUpload(file, "Validation error uploading profile image");
        String currentUser = userAuthentication.getEmail();
        ApplicationUser applicationUser = applicationUserRepository.findByEmail(currentUser);
        String profileImagePath = imageService.saveImage(file, PROFILE_IMAGES_DIR + applicationUser.getApplicationUserId());
        if (applicationUser.getProfileImage() == null) {
            ProfileImage profileImage = ProfileImage.builder()
                    .name(file.getOriginalFilename())
                    .path(profileImagePath)
                    .applicationUser(applicationUser).build();
            profileImageRepository.save(profileImage);
            applicationUser.setProfileImage(profileImage);
            applicationUserRepository.save(applicationUser);
        } else {
            ProfileImage profileImage = profileImageRepository.findById(applicationUser.getProfileImage().getProfileImageId())
                            .orElseThrow(() -> new NotFoundException("Can't locate profile image for user " + currentUser));
            String oldFilePath = profileImage.getPath();
            imageService.deleteOldFile(oldFilePath);
            profileImageRepository.updateProfileImage(profileImage.getProfileImageId(), profileImagePath, file.getOriginalFilename());
        }

        applicationUser = applicationUserRepository.findByEmail(currentUser);
        return applicationUser.getProfileImage().getPath();

    }

    @Override
    public UserEmailDto updateUserPassword(UserUpdatePasswordDto userUpdatePasswordDto) throws ValidationException {
        LOG.trace("updateUserPassword({})", userUpdatePasswordDto);
        String currentUser = userAuthentication.getEmail();
        applicationUserValidator.validatePasswordForUpdate(userUpdatePasswordDto, currentUser);
        applicationUserRepository.updatePasswordOnEmail(currentUser, passwordEncoder.encode(userUpdatePasswordDto.newPassword()));
        return new UserEmailDto(currentUser);
    }

    @Override
    public Page<UserDetailDto> searchUsers(UserSearchDto userSearchDto) {
        LOG.trace("searchingUsers({})", userSearchDto);
        Pageable pageable = PageRequest.of(userSearchDto.pageIndex(), userSearchDto.pageSize());

        Page<ApplicationUser> users = applicationUserRepository.findBySearch(
                userSearchDto.firstName(),
                userSearchDto.lastName(),
                userSearchDto.email(),
                userSearchDto.isAdmin(),
                userSearchDto.isLocked(),
                pageable
        );
        List<UserDetailDto> usersListDto = users.stream()
                .map(userMapper::applicationUserToUserDetailDto)
                .toList();

        return new PageImpl<>(usersListDto, pageable, users.getTotalElements());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException, BadCredentialsException {
        LOG.debug("Load all user by email");

        ApplicationUser user = applicationUserRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }
        if (user.isStudioAdmin() && !studioRepository.findByStudioAdminEmail(user.getEmail()).isApproved()) {
            throw new BadCredentialsException("Username or password is incorrect"); // don't allow login until approval
        }
        List<GrantedAuthority> grantedAuthorities;
        if (user.isAdmin()) {
            // Overall admin has all roles
            grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER");
        } else if (user.isStudioAdmin()) {
            // Studio admin has both user and studio admin roles
            grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_STUDIO_ADMIN");
        } else {
            // Regular user
            grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_USER");
        }

        if (user.getLoginAttempts() + 1 >= 5 && !user.isAdmin()) {
            blockUser(email);
            user = applicationUserRepository.findByEmail(email);
            mailService.sendUserBlockedEmail(user.getEmail(), user.getFirstName());
        } else {
            applicationUserRepository.setLoginAttempts(email, user.getLoginAttempts() + 1);
        }

        return new User(user.getEmail(), user.getPassword(), true, true, true, !user.isLocked(), grantedAuthorities);
    }

    @Override
    public void blockUser(String email)  {
        LOG.trace("blockUser({})", email);
        applicationUserRepository.updateIsLocked(email, true);
        applicationUserRepository.setLoginAttempts(email, 5);
    }

    @Override
    public UserEmailDto resetUserPassword(String email) throws NotFoundException, ValidationException {
        LOG.trace("resetUserPassword({})", email);
        userValidator.validateExistingEmail(email);
        String password = generateRandomPassword();
        applicationUserRepository.updatePasswordOnEmail(email, passwordEncoder.encode(password));
        var user = applicationUserRepository.findByEmail(email);
        if (user.isLocked()) {
            this.unblockUser(user.getEmail());
        }
        mailService.sendResetPasswordMail(email, user.getFirstName(), password);
        return new UserEmailDto(email);
    }

    private String generateRandomPassword() {
        LOG.debug("Generate random password");
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int passwordLength = 10;
        Random random = new SecureRandom();
        StringBuilder password = new StringBuilder(passwordLength);
        for (int i = 0; i < passwordLength; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }
        return password.toString();
    }


    @Override
    public UserDetailDto unblockUser(String email) throws ValidationException, NotFoundException {
        LOG.trace("unblockingUser({})", email);
        applicationUserValidator.validateOnUnblock(email, "Validation of user unblock failed");
        applicationUserRepository.updateIsLocked(email, false);
        applicationUserRepository.setLoginAttempts(email, 0);
        ApplicationUser user = applicationUserRepository.findByEmail(email);
        mailService.sendUserUnblockedEmail(email, user.getFirstName());
        return userMapper.applicationUserToUserDetailDto(applicationUserRepository.findByEmail(email));
    }

    @Override
    public List<UserInfoDto> searchFriends(UserSearchDto userSearchDto) {

        List<ApplicationUser> users = applicationUserRepository.searchUsers(
                userSearchDto.firstName(),
                userSearchDto.lastName(),
                userSearchDto.email(),
                PageRequest.of(0, 15)
        );

        // Map entities to DTOs
        return users.stream().map(user -> new UserInfoDto(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getLocation(),
                user.getGender(),
                user.getProfileImage() != null ? user.getProfileImage().getPath() : null
        )).toList();
    }

    @Override
    public List<UserInfoDto> searchMyFriends(UserSearchDto userSearchDto) {
        LOG.trace("searchMyFriends({})", userSearchDto);
        List<ApplicationUser> users = applicationUserRepository.searchMyFriends(
                userAuthentication.getEmail(),
                userSearchDto.firstName(),
                userSearchDto.lastName(),
                userSearchDto.email(),
                PageRequest.of(0, 15)
        );

        // Map entities to DTOs
        return users.stream().map(user -> new UserInfoDto(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getLocation(),
                user.getGender(),
                user.getProfileImage() != null ? user.getProfileImage().getPath() : null
        )).toList();
    }

    @Override
    public void deleteUserFriend(String email) {
        LOG.trace("deleteUserFriend({})", email);
        ApplicationUser user = this.applicationUserRepository
                .findByEmail(userAuthentication.getEmail());

        if (user == null) {
            throw new NotFoundException("User not found");
        }

        user.setFriends(
                user.getFriends()
                        .stream()
                        .filter(f -> !f.getEmail().equals(email))
                        .collect(Collectors.toSet())
        );

        applicationUserRepository.save(user);
    }

    @Override
    public List<StudioActivityDto> getRecommendations() {
        LOG.trace("getRecommendations()");
        ApplicationUser user = applicationUserRepository.findByEmail(userAuthentication.getEmail());
        List<StudioActivity> activities = studioActivityRepository.findRecommendedActivities(user.getApplicationUserId());
        return studioActivityMapper.entityToDto(activities);
    }

    @Override
    public List<StudioDto> getStudioRecommendations() {
        LOG.trace("getStudioRecommendations()");
        ApplicationUser user = applicationUserRepository.findByEmail(userAuthentication.getEmail());
        List<Studio> recommendedStudios = studioRepository.findRecommendedStudios(user.getApplicationUserId());
        return studioMapper.entityToDto(recommendedStudios);
    }

    @Override
    public List<StudioActivityDto> getActivities() {
        LOG.trace("getActivities()");
        ApplicationUser user = applicationUserRepository.findByEmail(userAuthentication.getEmail());
        List<StudioActivity> activities = user.getStudioActivities();
        return studioActivityMapper.entityToDto(activities);
    }
}
