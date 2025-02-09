package com.inso.sila.endpoint.mapper;

import com.inso.sila.endpoint.dto.requests.ActivityInvitationDto;
import com.inso.sila.entity.ActivityInvitation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityInvitationMapper {

    ActivityInvitation dtoToEntity(ActivityInvitationDto activityInvitation);

    ActivityInvitationDto entityToDto(ActivityInvitation activityInvitation);

    List<ActivityInvitationDto> entityToDtoList(List<ActivityInvitation> activityInvitations);

    List<ActivityInvitation> dtoToEntityList(List<ActivityInvitationDto> activityInvitationDtos);
}
