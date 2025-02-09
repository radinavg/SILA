package com.inso.sila.endpoint.mapper;

import com.inso.sila.endpoint.dto.image.GalleryImageDto;
import com.inso.sila.endpoint.dto.image.ProfileImageDto;
import com.inso.sila.entity.GalleryImage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GalleryImageMapper {

    GalleryImage dtoToEntity(GalleryImageDto galleryImageDto);

    GalleryImageDto entityToDto(GalleryImage galleryImage);

    List<ProfileImageDto> entityToDtoList(List<GalleryImage> galleryImages);

    List<GalleryImage> dtoToEntityList(List<GalleryImageDto> galleryImageDtos);
}
