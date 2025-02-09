package com.inso.sila.endpoint.dto.studio.studio;

import com.inso.sila.endpoint.dto.image.GalleryImageDto;
import com.inso.sila.endpoint.dto.image.ProfileImageDto;
import com.inso.sila.endpoint.dto.studio.faqs.FaqsDto;
import com.inso.sila.endpoint.dto.studio.instructor.InstructorDto;
import com.inso.sila.endpoint.dto.studio.membership.MembershipDto;
import lombok.Builder;


import java.util.List;

@Builder
public record StudioDto(
        Long studioId,
        ProfileImageDto profileImage,
        List<GalleryImageDto> galleryImages,
        String name,
        String description,
        String location,
        Integer reviewsLength,
        double averageReview,
        List<FaqsDto> faqs,
        boolean approved,
        boolean isFavouriteForUser,
        List<InstructorDto> instructors,
        List<MembershipDto> memberships


) {
}
