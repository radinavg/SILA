package com.inso.sila.repository;

import com.inso.sila.entity.ActivityInvitation;
import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.StudioActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityInvitationRepository extends JpaRepository<ActivityInvitation, Long> {

    @Query("SELECT ai FROM ActivityInvitation ai "
            + "WHERE ai.to.email = :email AND NOT ai.seen "
            + "ORDER BY ai.requestDateTime DESC")
    List<ActivityInvitation> findAllUnseenActivityInvitations(@Param("email") String email);


    List<ActivityInvitation> findAllByFrom(ApplicationUser from);

    ActivityInvitation findByFromAndToAndStudioActivity(ApplicationUser byEmail, ApplicationUser toUser, StudioActivity studioActivity);
}
