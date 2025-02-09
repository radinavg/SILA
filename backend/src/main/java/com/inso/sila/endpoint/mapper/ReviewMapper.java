package com.inso.sila.endpoint.mapper;

import com.inso.sila.endpoint.dto.studio.review.ReviewDto;
import com.inso.sila.entity.Review;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring")
public interface ReviewMapper {

    Review dtoToReview(ReviewDto dto);

    ReviewDto reviewToDto(Review review);

    List<ReviewDto> reviewsToDtos(List<Review> reviews);

    List<Review> reviewDtosToReviews(List<ReviewDto> reviewDtos);
}
