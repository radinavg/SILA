package com.inso.sila.repository;

import com.inso.sila.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r SET r.text = :text, r.rating = :rating, r.createdAt = :createdAt WHERE r.reviewId = :reviewId")
    void updateReview(@Param("reviewId") Long reviewId, @Param("text") String text, @Param("rating") float rating, @Param("createdAt") LocalDateTime createdAt);

    Review findByReviewId(Long reviewId);

    @Query(value = "SELECT r FROM Review r "
            + "WHERE r.studio.studioId = :studioId "
            + "ORDER BY "
            + "CASE WHEN r.user.email = :currentUserEmail THEN 0 ELSE 1 END, "
            + "r.createdAt DESC, "
            + "r.rating ASC")
    Page<Review> findSortedReviewsByStudio(
            @Param("studioId") Long studioId,
            @Param("currentUserEmail") String currentUserEmail,
            Pageable pageable
    );

    @Query("SELECT r FROM Review r WHERE r.studio.studioId = :studioId")
    List<Review> findByStudioId(@Param("studioId") Long studioId);


}
