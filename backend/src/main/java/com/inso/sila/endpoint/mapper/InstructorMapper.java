package com.inso.sila.endpoint.mapper;

import com.inso.sila.endpoint.dto.studio.instructor.InstructorDto;
import com.inso.sila.entity.Instructor;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface InstructorMapper {

    InstructorDto entityToDto(Instructor instructor);

    Instructor dtoToEntity(InstructorDto instructorDto);

}
