package com.inso.sila.endpoint.mapper;

import com.inso.sila.endpoint.dto.studioactivity.StudioActivityPreferencesDto;
import com.inso.sila.entity.StudioActivityPreferences;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface StudioActivityPreferencesMapper {

    StudioActivityPreferencesDto entityToDto(StudioActivityPreferences studioActivityPreferences);

    StudioActivityPreferences dtoToEntity(StudioActivityPreferencesDto studioActivityPreferencesDto);

}
