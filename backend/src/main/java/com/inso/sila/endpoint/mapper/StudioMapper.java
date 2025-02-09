package com.inso.sila.endpoint.mapper;


import com.inso.sila.endpoint.dto.studio.studio.CreatedStudioDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioForActivityDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioInfoDto;
import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.Review;
import com.inso.sila.entity.Studio;
import com.inso.sila.repository.ReviewRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {ProfileImageMapper.class, GalleryImageMapper.class})
public abstract class StudioMapper {

    @Autowired
    private ReviewRepository reviewRepository;

    @Mapping(target = "isFavouriteForUser",
            expression = "java(studio.getLikedFromApplicationUsers() != null && studio.getLikedFromApplicationUsers().contains(currentUser))")
    @Mapping(target = "reviewsLength", source = "studio", qualifiedByName = "mapReviewsLength")
    @Mapping(target = "averageReview", source = "studio", qualifiedByName = "mapAverageReview")
    public abstract StudioDto entityToDto(Studio studio, @Context ApplicationUser currentUser);

    public abstract StudioDto entityToDto(Studio studio);

    public abstract List<StudioDto> entityToDto(List<Studio> studios);

    public abstract StudioForActivityDto entityToStudioDtoForActivity(Studio studio);

    @Named("mapReviewsLength")
    public Integer mapReviewsLength(Studio studio) {
        List<Review> reviews = reviewRepository.findByStudioId(studio.getStudioId());
        return reviews != null ? reviews.size() : 0;
    }

    @Named("mapAverageReview")
    public double mapAverageReview(Studio studio) {
        List<Review> reviews = reviewRepository.findByStudioId(studio.getStudioId());
        if (reviews != null && !reviews.isEmpty()) {
            return reviews.stream().mapToDouble(Review::getRating).average().orElse(0.0f);
        }
        return 0f;
    }

    public abstract List<Studio> dtoToEntity(List<StudioDto> studioDtos);

    public abstract Studio dtoToEntity(StudioDto studioDto);

    @Named("mapFavouriteStudio")
    public boolean mapFavouriteStudio(Studio studio, ApplicationUser currentUser) {
        if (currentUser == null || studio.getLikedFromApplicationUsers() == null) {
            return false;
        }
        return studio.getLikedFromApplicationUsers().contains(currentUser);
    }

    @Mapping(source = "profileImage.path", target = "profileImagePath")
    @Mapping(source = "studioAdmin.email", target = "email")
    public abstract StudioInfoDto entityToInfoDto(Studio studio);

    public abstract CreatedStudioDto entityToCreatedStudioDto(Studio studio);
}

