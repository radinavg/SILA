package com.inso.sila.service.impl;

import com.inso.sila.endpoint.dto.studio.review.ReviewCreateDto;
import com.inso.sila.endpoint.dto.studio.review.ReviewDto;
import com.inso.sila.endpoint.dto.studio.review.ReviewSortDto;
import com.inso.sila.endpoint.mapper.ReviewMapper;
import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.Review;
import com.inso.sila.entity.Studio;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.ReviewRepository;
import com.inso.sila.repository.StudioRepository;
import com.inso.sila.security.UserAuthentication;
import com.inso.sila.service.ReviewService;
import com.inso.sila.service.validation.ApplicationUserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ReviewRepository reviewRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final StudioRepository studioRepository;
    private final ReviewMapper reviewMapper;
    private final UserAuthentication userAuthentication;
    private final ApplicationUserValidator applicationUserValidator;
    private final WebClient webClient;
    private static final String DATASCIENCE_API = "http://datascience:5000";

    public ReviewServiceImpl(ReviewRepository reviewRepository, ApplicationUserRepository applicationUserRepository,
                             StudioRepository studioRepository, ReviewMapper reviewMapper,
                             UserAuthentication userAuthentication, ApplicationUserValidator applicationUserValidator,
                             WebClient.Builder builder) {
        this.reviewRepository = reviewRepository;
        this.applicationUserRepository = applicationUserRepository;
        this.studioRepository = studioRepository;
        this.reviewMapper = reviewMapper;
        this.userAuthentication = userAuthentication;
        this.applicationUserValidator = applicationUserValidator;
        this.webClient = builder.baseUrl(DATASCIENCE_API).build();
    }


    @Override
    public ReviewDto addReview(ReviewCreateDto reviewDto, Long studioId) {
        LOG.trace("addReview({}, {})", reviewDto, studioId);

        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio with id %d not found.".formatted(studioId)));
        ApplicationUser applicationUser = applicationUserRepository.findByEmail(userAuthentication.getEmail());
        Review review = Review.builder()
                .rating(reviewDto.rating())
                .text(reviewDto.text())
                .createdAt(LocalDateTime.now())
                .studio(studio)
                .user(applicationUser)
                .build();

        studioRepository.save(studio);
        Review savedReview = reviewRepository.save(review);
        reviewRepository.save(review);
        String responseFromPython = webClient.get()
                .uri("/collaborative-filtering/{user_id}", applicationUser.getApplicationUserId())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        LOG.info("Response from Python service: {}", responseFromPython);

        return reviewMapper.reviewToDto(savedReview);
    }

    @Override
    public ReviewDto deleteReview(Long reviewId) throws NotFoundException, SecurityException {
        LOG.trace("deleteReview({})", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review with ID " + reviewId + " not found"));

        applicationUserValidator.validateAuthorization(userAuthentication.getEmail(), review.getUser().getEmail());

        reviewRepository.delete(review);
        return reviewMapper.reviewToDto(review);
    }

    @Override
    public ReviewDto updateReview(Long reviewId, ReviewCreateDto review) {
        LOG.trace("updateReview({})", review);

        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review with ID " +  reviewId + " not found"));

        applicationUserValidator.validateAuthorization(userAuthentication.getEmail(), existingReview.getUser().getEmail());
        reviewRepository.updateReview(reviewId, review.text(), review.rating(), LocalDateTime.now());
        Review updatedReview = reviewRepository.findByReviewId(reviewId);
        return reviewMapper.reviewToDto(updatedReview);
    }

    @Override
    public Page<ReviewDto> sortReviews(Long studioId, ReviewSortDto reviewSearchDto) {
        LOG.trace("searchReviews({}, {})", studioId, reviewSearchDto);
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new NotFoundException("Studio with id %d not found.".formatted(studioId)));
        Pageable pageable = PageRequest.of(reviewSearchDto.pageIndex(), reviewSearchDto.pageSize());


        Page<Review> reviews = reviewRepository.findSortedReviewsByStudio(
                studio.getStudioId(),
                userAuthentication.getEmail(),
                PageRequest.of(reviewSearchDto.pageIndex(), reviewSearchDto.pageSize())
        );

        List<ReviewDto> reviewListDto = reviews.stream()
                .map(reviewMapper::reviewToDto)
                .toList();

        return new PageImpl<>(reviewListDto, pageable, reviews.getTotalElements());
    }

}
