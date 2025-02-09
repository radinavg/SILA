package com.inso.sila.service;

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
import com.inso.sila.exception.ConflictException;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface ApplicationUserService {


    /**
     * Log in a user.
     *
     * @param userLoginDto login credentials
     * @return the JWT, if successful
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are bad
     * @throws org.springframework.security.authentication.LockedException         if credentials user is blocked
     */
    String login(UserLoginDto userLoginDto) throws BadCredentialsException, LockedException;

    /**
     * Find a user in the context of Spring Security based on the email address.
     * For more information have a look at this tutorial:
     * <a href="https://www.baeldung.com/spring-security-authentication-with-a-database">Baeldung Spring Security</a>
     *
     * @param email the email address
     * @return a Spring Security user
     * @throws UsernameNotFoundException is thrown if the specified user does not exists
     */
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

    /**
     * Block a user identified by the specified email address.
     *
     * @param email the email address of the user to unblock
     */
    void blockUser(String email);

    /**
     * Reset user password (admin permission).
     *
     * @param email user instance dto
     * @return updated user inside a record (user password reset is sent to user via mail)
     * @throws NotFoundException if the user does not exist
     */
    UserEmailDto resetUserPassword(String email) throws NotFoundException, ValidationException;

    /**
     * Registering user and returning user email.
     *
     * @param userRegisterDto userRegisterDto
     * @return the UserEmailDto DTO
     */
    UserEmailDto registerUser(UserRegisterDto userRegisterDto) throws ConflictException, ValidationException;

    /**
     * Creating user preferences.
     *
     * @param userPreferencesDto userPreferencesDto
     * @return the UserInfoDto DTO
     */
    UserInfoDto updateUserPreferences(UserPreferencesDto userPreferencesDto);

    /**
     * Check if user preferences exist.
     *
     * @return true if user preferences exist, false otherwise
     * @throws NotFoundException if the user email does not exist
     */
    boolean checkPreferences();

    /**
     * Updating user preferences.
     *
     * @param userPreferencesDto userPreferencesDto
     * @return the UserInfoDto DTO
     */
    UserInfoDto createUserPreferences(UserPreferencesDto userPreferencesDto) throws ConflictException;

    /**
     * Get user information.
     *
     * @param email users email
     * @return user information inside record
     * @throws NotFoundException if the user email does not exist
     */
    UserInfoDto getUserInfo(String email) throws NotFoundException, SecurityException;

    /**
     * Update user information.
     *
     * @param userInfoDto user information to be changed
     * @return updated user information inside record
     * @throws SecurityException if the user is not authorized to take the action or is blocked
     */
    UserInfoDto updateUserInfo(UpdateUserInfoDto userInfoDto) throws SecurityException;

    /**
     * Delete user.
     *
     * @param email user email (unique)
     * @return deleted user information inside record
     * @throws NotFoundException if the user email does not exist
     * @throws SecurityException if the user authentication does not match the request user
     */
    UserInfoDto deleteUser(String email) throws SecurityException, ConflictException;

    /**
     * Uploads images to the backend server for the current user.
     *
     * @param file profile image to upload
     * @return String of path to saved path image.
     * @throws IOException when saving images with FileStorageService fails
     * @throws ValidationException when uploading images fails due to validation errors on the image.
     * */
    String uploadUserProfileImage(MultipartFile file) throws IOException, ValidationException;

    /**
     * Update user password (user permission).
     *
     * @param userUpdatePasswordDto containing current password, new password and confirmation of new password
     * @return user instance dto
     * @throws ValidationException if the data fields are not valid or missing
     */
    UserEmailDto updateUserPassword(UserUpdatePasswordDto userUpdatePasswordDto) throws ValidationException;

    /**
     * Search for users based on the provided search criteria.
     *
     * @param userSearchDto the DTO containing the search criteria (firstName, lastName, email, isAdmin, isLocked)
     * @return a list of UserListDto objects representing the users that match the search criteria
     */
    Page<UserDetailDto> searchUsers(UserSearchDto userSearchDto);


    /**
     * Unblock a user identified by the specified email address.
     *
     * @param email the email address of the user to unblock
     * @return the UserDetailDto representing the unblocked user
     * @throws ValidationException if user is actually not blocked or their email does not exist
     */
    UserDetailDto unblockUser(String email) throws ValidationException, NotFoundException;

    List<StudioActivityDto> getActivities();

    List<StudioActivityDto> getRecommendations();

    List<StudioDto> getStudioRecommendations();

    List<UserInfoDto> searchFriends(UserSearchDto userSearchDto);

    List<UserInfoDto> searchMyFriends(UserSearchDto userSearchDto);

    void deleteUserFriend(String email);
}
