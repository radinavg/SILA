package com.inso.sila.endpoint;

import com.inso.sila.endpoint.dto.studio.review.ReviewCreateDto;
import com.inso.sila.endpoint.dto.studio.review.ReviewDto;
import com.inso.sila.endpoint.dto.studio.review.ReviewSortDto;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.service.ReviewService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(value = "/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ReviewService reviewService;

    @Secured("ROLE_USER")
    @PostMapping("/{studioId}")
    public ResponseEntity<ReviewDto> addReview(@PathVariable Long studioId, @RequestBody @Valid ReviewCreateDto review) {
        LOG.info("Add review: {}", review);
        try {
            ReviewDto reviewDto = reviewService.addReview(review, studioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(reviewDto);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Unknown studio to add review to", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_USER")
    @GetMapping("/{studioId}")
    public Page<ReviewDto> sortReviews(@PathVariable Long studioId, ReviewSortDto search) {
        LOG.info("Search reviews: {}", search);
        try {
            return reviewService.sortReviews(studioId, search);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Unknown studio to search reviews", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @Secured("ROLE_USER")
    @DeleteMapping("/{reviewId}")
    public ReviewDto deleteReview(@PathVariable Long reviewId) {
        LOG.info("Delete review: {}", reviewId);
        try {
            return reviewService.deleteReview(reviewId);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Unknown review to delete", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (SecurityException e) {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            logClientError(status, "Unauthorized action", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @PermitAll
    @PutMapping("/{reviewId}")
    public ReviewDto updateReview(@PathVariable Long reviewId, @RequestBody @Valid ReviewCreateDto review) {
        LOG.info("Update review: {}", review);
        try {
            return reviewService.updateReview(reviewId, review);
        }  catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Unknown review to update", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (SecurityException e) {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            logClientError(status, "Unauthorized action", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    private void logClientError(HttpStatus status, String message, Exception e) {
        LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
    }


}
