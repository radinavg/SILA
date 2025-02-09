package com.inso.sila.service;

import com.inso.sila.endpoint.dto.studio.review.ReviewCreateDto;
import com.inso.sila.endpoint.dto.studio.review.ReviewDto;
import com.inso.sila.endpoint.dto.studio.review.ReviewSortDto;
import com.inso.sila.exception.NotFoundException;
import org.springframework.data.domain.Page;

public interface ReviewService {
    ReviewDto addReview(ReviewCreateDto review, Long studioId) throws NotFoundException;

    ReviewDto deleteReview(Long reviewId) throws NotFoundException, SecurityException;

    ReviewDto updateReview(Long reviewId, ReviewCreateDto review) throws NotFoundException, SecurityException;

    Page<ReviewDto> sortReviews(Long studioId, ReviewSortDto reviewSearchDto) throws NotFoundException;
}
