package com.inso.sila.repository;

import com.inso.sila.entity.StudioActivity;
import com.inso.sila.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface StudioActivityRepository extends JpaRepository<StudioActivity, Long> {

    void deleteStudioActivityByStudioActivityId(@Param("studioActivityId") Long studioActivityId);

    @Query("SELECT sa FROM StudioActivity sa WHERE "
            + "sa.type = :type AND sa.dateTime > CURRENT_TIMESTAMP "
            + "ORDER BY sa.dateTime"
    )
    Page<StudioActivity> findStudioActivitiesByType(@Param("type") ActivityType type, Pageable pageable);

    @Query("SELECT sa FROM StudioActivity sa "
            + "JOIN Studio s ON sa MEMBER OF s.studioActivities "
            + "WHERE s.studioId = :studioId AND sa.dateTime > CURRENT_TIMESTAMP "
            + "ORDER BY sa.dateTime ASC")
    List<StudioActivity> findCurrentSortedStudioActivities(@Param("studioId") Long studioId);

    /*
    SELECT sa FROM studio_activity sa
            JOIN recommendation_cluster_recommended_activities rcra ON sa.studio_activity_id = rcra.recommended_activities_studio_activity_id
            JOIN application_user au ON rcra.recommendation_cluster_recommendation_cluster_id = au.recommendation_cluster_id
            WHERE au.application_user_id = 25;
     */
    @Query("SELECT sa FROM StudioActivity sa "
            + "JOIN RecommendationCluster rcra ON sa MEMBER OF rcra.recommendedActivities "
            + "JOIN ApplicationUser au ON rcra.recommendationClusterId = au.recommendationCluster.recommendationClusterId "
            + "WHERE au.applicationUserId = :applicationUserId")
    List<StudioActivity> findRecommendedActivities(@Param("applicationUserId") Long applicationUserId);


    @Query("SELECT COUNT(au) FROM ApplicationUser au "
            + "JOIN StudioActivity sa ON au MEMBER OF sa.applicationUsers "
            + "WHERE sa.studioActivityId = :studioActivityId")
    Long countBookedPlacesForActivity(@Param("studioActivityId") Long studioActivityId);

}
