package com.inso.sila.repository;

import com.inso.sila.entity.Studio;
import com.inso.sila.entity.StudioActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Repository
public interface StudioRepository extends JpaRepository<Studio, Long> {

    List<Studio> findByApprovedFalse();

    List<Studio> findByApprovedTrue();

    @Query("SELECT s FROM Studio s WHERE "
            + "s.approved = true AND "
            + "(UPPER(s.name) LIKE UPPER(CONCAT('%', :name, '%')) OR :name IS NULL) AND "
            + "(UPPER(s.location) LIKE UPPER(CONCAT('%', :location, '%')) OR :location IS NULL ) "
            + "ORDER BY s.name ASC"
    )
    Page<Studio> findByNameAndLocation(@Param("name") String name, @Param("location") String location, Pageable pageable);

    @Query("SELECT s FROM Studio s JOIN s.studioActivities sa WHERE sa = :studioActivity")
    Studio findStudioByStudioActivity(@Param("studioActivity") StudioActivity studioActivity);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Studio s SET s.approved = :approved WHERE s.studioId = :studioId")
    void updateApprovalOfStudio(@Param("studioId") Long studioId, @Param("approved") Boolean approved);

    @Query("SELECT s FROM Studio s WHERE s.studioAdmin.email = :studioAdminEmail")
    Studio findByStudioAdminEmail(@Param("studioAdminEmail") String studioAdminEmail);

    @Query("SELECT s FROM Studio s JOIN s.studioActivities sa WHERE sa.studioActivityId = :studioActivityId")
    Studio findByStudioActivityId(@Param("studioActivityId") Long studioActivityId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Studio s SET s.name = :name, s.location = :location, s.latitude = :latitude, s.longitude = :longitude, "
            + "s.description = :description WHERE s.studioId = :studioId")
    void updateStudio(@Param("studioId") Long studioId,
                      @Param("name") String name,
                      @Param("description") String description,
                      @Param("location") String location,
                      @Param("longitude") float longitude,
                      @Param("latitude") float latitude);

    Studio findByStudioId(Long studioId);

    @Query("SELECT s FROM Studio s LEFT JOIN FETCH s.studioAdmin LEFT JOIN FETCH s.likedFromApplicationUsers WHERE s.studioId = :studioId")
    Optional<Studio> findByIdWithRelations(@Param("studioId") Long studioId);

    Studio findByLocation(String location);

    @Query("SELECT DISTINCT s FROM Studio s "
            + "JOIN s.studioActivities sa "
            + "JOIN sa.applicationUsers au "
            + "WHERE au.applicationUserId = :applicationUserId")
    List<Studio> findStudiosForWhichUserHasBookings(@Param("applicationUserId") Long applicationUserId);

    @Query("SELECT s FROM ApplicationUser u "
            + "JOIN u.studioRecommendations s "
            + "WHERE u.applicationUserId = :applicationUserId")
    List<Studio> findRecommendedStudios(@Param("applicationUserId") Long applicationUserId);

}
