package com.inso.sila.endpoint.mapper;

import com.inso.sila.endpoint.dto.image.ProfileImageDto;
import com.inso.sila.entity.ProfileImage;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring")
public interface ProfileImageMapper {

    ProfileImage dtoToEntity(ProfileImageDto profileImageDto);

    ProfileImageDto entityToDto(ProfileImage profileImage);

    List<ProfileImageDto> entityToDtoList(List<ProfileImage> profileImages);

    List<ProfileImage> dtoToEntityList(List<ProfileImageDto> profileImageDtos);
}
