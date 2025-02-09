package com.inso.sila.endpoint.mapper;

import com.inso.sila.endpoint.dto.user.UserDetailDto;
import com.inso.sila.endpoint.dto.user.UserInfoDto;
import com.inso.sila.entity.ApplicationUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "location", source = "location")
    @Mapping(source = "profileImage.path", target = "profileImagePath")
    UserInfoDto userToUserInfoDto(ApplicationUser applicationUserByEmail);

    @Mapping(source = "profileImage.path", target = "profileImagePath")
    @Mapping(source = "locked", target = "isLocked")
    @Mapping(source = "admin", target = "isAdmin")
    @Mapping(source = "studioAdmin", target = "isStudioAdmin")
    UserDetailDto applicationUserToUserDetailDto(ApplicationUser applicationUser);

    @Mapping(source = "profileImage.path", target = "profileImagePath")
    @Mapping(source = "locked", target = "isLocked")
    List<UserDetailDto> applicationUserToUserRegisterDtoList(List<ApplicationUser> applicationUserList);

    ApplicationUser userInfoDtoToApplicationUser(UserInfoDto userDetailDto);

    @Mapping(target = "location", source = "location")
    @Mapping(source = "profileImage.path", target = "profileImagePath")
    List<UserInfoDto> usersToUserInfoDtos(List<ApplicationUser> users);

}
