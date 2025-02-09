package com.inso.sila.endpoint.mapper;

import com.inso.sila.endpoint.dto.requests.FriendRequestDto;
import com.inso.sila.entity.FriendRequest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FriendRequestMapper {

    FriendRequest dtoToEntity(FriendRequestDto friendRequestDto);

    List<FriendRequest> dtoToEntity(List<FriendRequestDto> friendRequestDtos);

    FriendRequestDto entityToDto(FriendRequest friendRequest);

    List<FriendRequestDto> entityToDto(List<FriendRequest> friendRequests);


}
