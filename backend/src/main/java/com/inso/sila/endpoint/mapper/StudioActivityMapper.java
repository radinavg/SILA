package com.inso.sila.endpoint.mapper;

import com.inso.sila.endpoint.dto.studio.studio.StudioActivityListDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityCreateDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityTypeSearchResponseDto;
import com.inso.sila.entity.StudioActivity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring", uses = {ProfileImageMapper.class})
public interface StudioActivityMapper {


    List<StudioActivityDto> entityToDto(List<StudioActivity> studioActivities);

    StudioActivityDto entityToDto(StudioActivity studioActivity);

    StudioActivity dtoToEntity(StudioActivityDto studioActivityDto);

    List<StudioActivity> dtoToEntity(List<StudioActivityDto> studioActivityDtos);

    StudioActivity dtoToEntity(StudioActivityCreateDto studioActivityCreateDto);

    @Mapping(source = "profileImage.path", target = "profileImagePath")
    StudioActivityTypeSearchResponseDto studioActivityEntityToTyeSearchResponseDto(StudioActivity studioActivity);

    StudioActivityListDto studioActivityEntityToListDto(StudioActivity studioActivity);
}
