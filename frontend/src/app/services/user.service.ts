import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {environment} from "../../environments/environment";
import {
    ResetUserPasswordDto,
    User,
    UserCreateDto,
    UserDetailDto,
    UserInfoDto, UserPreferencesDto, UserSearchDto,
    UserUpdatePasswordDto
} from "../dtos/user";
import {Observable} from "rxjs";
import {PaginatedResponse} from "../dtos/paginator";
import {StudioActivity} from "../dtos/studioActivity";
import {StudioDto} from "../dtos/studio";

const baseUri = environment.backendUrl + '/api/v1/users';

@Injectable({
    providedIn: 'root'
})
export class UserService {

    constructor(
        private http: HttpClient,
    ) {
    }


    /**
     * Get user information by email.
     *
     * @param email the email of the user to retrieve information for
     * @return an Observable with the user information
     */
    getUserInfo(email: String): Observable<UserInfoDto> {
        return this.http.get<UserInfoDto>(baseUri + `/info/${email}`, {});
    }

    /**
     * Reset user password.
     *
     * @param resetPasswordDto the details required to reset the password
     * @return an Observable with the reset password details
     */
    resetPassword(resetPasswordDto: ResetUserPasswordDto): Observable<ResetUserPasswordDto> {
        return this.http.put<ResetUserPasswordDto>(baseUri + `/reset/password`, resetPasswordDto);
    }

    /**
     * Register a new user.
     *
     * @param registrationDetail the details of the user to register
     * @return an Observable with the registered user details
     */
    registerUser(registrationDetail: UserCreateDto): Observable<UserCreateDto> {
        console.log(registrationDetail);
        return this.http.post<UserCreateDto>(baseUri + '/create', registrationDetail)
    }

    /**
     * Update user information.
     *
     * @param userInfo the updated user information
     * @return an Observable with the updated user information
     */
    updateUserInfo(userInfo: UserInfoDto): Observable<UserInfoDto> {
        return this.http.put<UserInfoDto>(
            baseUri + '/update',
            userInfo);
    }

    /**
     * Delete a user by email.
     *
     * @param userEmail the email of the user to delete
     * @return an Observable with the deleted user information
     */
    deleteUser(userEmail: String): Observable<UserInfoDto> {
        return this.http.delete<UserInfoDto>(
            baseUri + `/delete/${userEmail}`,
            {}
        )
    }

    /**
     * Update user preferences.
     *
     * @param preferences the user preferences to update
     * @return an Observable with the updated user preferences
     */
    updateUserPreferences(preferences: UserPreferencesDto): Observable<UserPreferencesDto> {
        return this.http.put<UserPreferencesDto>(baseUri + '/update-preferences', preferences);
    }

    /**
     * Create user preferences.
     *
     * @param preferences the user preferences to create
     * @return an Observable with the created user preferences
     */
    createUserPreferences(preferences: UserPreferencesDto): Observable<UserPreferencesDto> {
        console.log(preferences);
        return this.http.post<UserPreferencesDto>(baseUri + '/create-preferences', preferences);
    }

    /**
     * Check if user preferences are set.
     *
     * @returns an Observable with a boolean indicating if the user preferences are set
     */
    checkPreferencesSet(): Observable<boolean> {
        return this.http.get<boolean>(`${baseUri}/preferences-check`);
    }

    /**
     * Uploads a profile image for the user.
     *
     * This method sends a `multipart/form-data` request containing the selected image
     * file to the server's `/users/upload-profile-image` endpoint. The server is expected
     * to process the file, store it, and return the URL of the uploaded image in the response.
     *
     * @param formData - A FormData object containing the image file for user profile image
     * @param headers - signalizing Multipart File content upload
     * @returns An Observable of string containing the path zu image
     */
    uploadProfileImage(formData: FormData, headers: HttpHeaders): Observable<string> {
        return this.http.put<string>(`${baseUri}/upload-profile-image`, formData,
            {
                headers,
                responseType: 'text' as 'json'
            });
    }

    /**
     * Update user password.
     *
     * @param password the new password details
     * @return an Observable with the updated password details
     */
    updateUserPassword(password: UserUpdatePasswordDto): Observable<User> {
        return this.http.put<User>(baseUri + '/update/password', password);
    }

    getAllUsers(searchParams: UserSearchDto): Observable<PaginatedResponse<UserDetailDto>> {
        let params = new HttpParams();

        if (searchParams.firstName) {
            params = params.append("firstName", searchParams.firstName);
        }

        if (searchParams.lastName) {
            params = params.append("lastName", searchParams.lastName);
        }

        if (searchParams.email) {
            params = params.append("email", searchParams.email);
        }

        if (searchParams.isLocked === true || searchParams.isLocked === false) {
            params = params.append("isLocked", searchParams.isLocked);
        }

        if (searchParams.isAdmin === true || searchParams.isAdmin === false) {
            params = params.append("isAdmin", searchParams.isAdmin);
        }
        params = params.append('pageIndex', searchParams.pageIndex.toString());
        params = params.append('pageSize', searchParams.pageSize.toString());

        return this.http.get<PaginatedResponse<UserDetailDto>>(baseUri + '/search', {params});
    }

    /**
     * Admin action to unblock user that has been blocked due to many login attempts.
     *
     * @param email of user to be unblocked
     * @return an observable of user that has been unblocked
     */
    unblockUser(email: String): Observable<UserDetailDto> {
        return this.http.put<UserDetailDto>(baseUri + `/unblock/${email}`, {});
    }

    getActivities(): Observable<StudioActivity[]> {
        return this.http.get<StudioActivity[]>(baseUri + `/activities`);
    }

    getRecommendations(): Observable<StudioActivity[]> {
        return this.http.get<StudioActivity[]>(baseUri + `/recommendations`);
    }

    getStudioRecommendations(): Observable<StudioDto[]> {
      return this.http.get<StudioDto[]>(baseUri + `/studio-recommendations`);
    }

    searchFriends(searchQuery: UserSearchDto): Observable<UserInfoDto[]> {
        let params = new HttpParams();

        if (searchQuery.firstName) {
            params = params.append('firstName', searchQuery.firstName);
        }
        if (searchQuery.lastName) {
            params = params.append('lastName', searchQuery.lastName);
        }
        if (searchQuery.email) {
            params = params.append('email', searchQuery.email);
        }

        return this.http.get<UserInfoDto[]>(baseUri + '/search-friends', {params})
    }


    getFriendsForUser(userSearchDto: UserSearchDto): Observable<UserInfoDto[]> {

        let params = new HttpParams();

        if (userSearchDto.firstName) {
            params = params.append('firstName', userSearchDto.firstName);
        }
        if (userSearchDto.lastName) {
            params = params.append('lastName', userSearchDto.lastName);
        }
        if (userSearchDto.email) {
            params = params.append('email', userSearchDto.email);
        }
        return this.http.get<UserInfoDto[]>(baseUri + '/my-friends', {params})
    }


    deleteFriend(friendEmail: string): Observable<void> {
        return this.http.delete<void>(baseUri + '/friend/' + friendEmail);
    }

    searchMyFriends(searchQuery: UserSearchDto): Observable<UserInfoDto[]> {
        let params = new HttpParams();

        if (searchQuery.firstName) {
            params = params.append('firstName', searchQuery.firstName);
        }
        if (searchQuery.lastName) {
            params = params.append('lastName', searchQuery.lastName);
        }
        if (searchQuery.email) {
            params = params.append('email', searchQuery.email);
        }

        return this.http.get<UserInfoDto[]>(baseUri + '/search-my-friends', {params})
    }

}
