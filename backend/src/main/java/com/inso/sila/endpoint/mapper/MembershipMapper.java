package com.inso.sila.endpoint.mapper;

import com.inso.sila.endpoint.dto.studio.membership.MembershipDto;
import com.inso.sila.entity.Membership;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring")
public interface MembershipMapper {

    MembershipDto entityToDto(Membership membership);

    Membership membershipDtoToEntity(MembershipDto membershipDto);

    List<Membership> membershipDtoToEntityList(List<MembershipDto> membershipDtoList);

    List<MembershipDto> entityListToDtoList(List<Membership> membershipList);
}
