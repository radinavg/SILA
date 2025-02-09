package com.inso.sila.repository;

import com.inso.sila.entity.ApplicationUserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ApplicationUserPreferencesRepository extends JpaRepository<ApplicationUserPreferences, Integer> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ApplicationUserPreferences u SET "
            + "u.prefersIndividual = :prefersIndividual, "
            + "u.prefersTeam = :prefersTeam, "
            + "u.prefersWaterBased = :prefersWaterBased, "
            + "u.prefersIndoor = :prefersIndoor, "
            + "u.prefersOutdoor = :prefersOutdoor, "
            + "u.prefersBothIndoorAndOutdoor = :prefersBothIndoorAndOutdoor, "
            + "u.prefersWarmClimate = :prefersWarmClimate, "
            + "u.prefersColdClimate = :prefersColdClimate, "
            + "u.rainCompatibility = :rainCompatibility, "
            + "u.windSuitability = :windSuitability, "
            + "u.focusUpperBody = :focusUpperBody, "
            + "u.focusLowerBody = :focusLowerBody, "
            + "u.focusCore = :focusCore, "
            + "u.focusFullBody = :focusFullBody, "
            + "u.isBeginner = :isBeginner, "
            + "u.isIntermediate = :isIntermediate, "
            + "u.isAdvanced = :isAdvanced, "
            + "u.physicalDemandLevel = :physicalDemandLevel, "
            + "u.goalStrength = :goalStrength, "
            + "u.goalEndurance = :goalEndurance, "
            + "u.goalFlexibility = :goalFlexibility, "
            + "u.goalBalanceCoordination = :goalBalanceCoordination, "
            + "u.goalMentalFocus = :goalMentalFocus "
            + "WHERE u.id = :id")
    void updateUserPreferences(
            @Param("id") Long id,
            @Param("prefersIndividual") boolean prefersIndividual,
            @Param("prefersTeam") boolean prefersTeam,
            @Param("prefersWaterBased") boolean prefersWaterBased,
            @Param("prefersIndoor") boolean prefersIndoor,
            @Param("prefersOutdoor") boolean prefersOutdoor,
            @Param("prefersBothIndoorAndOutdoor") boolean prefersBothIndoorAndOutdoor,
            @Param("prefersWarmClimate") boolean prefersWarmClimate,
            @Param("prefersColdClimate") boolean prefersColdClimate,
            @Param("rainCompatibility") boolean rainCompatibility,
            @Param("windSuitability") boolean windSuitability,
            @Param("focusUpperBody") boolean focusUpperBody,
            @Param("focusLowerBody") boolean focusLowerBody,
            @Param("focusCore") boolean focusCore,
            @Param("focusFullBody") boolean focusFullBody,
            @Param("isBeginner") boolean isBeginner,
            @Param("isIntermediate") boolean isIntermediate,
            @Param("isAdvanced") boolean isAdvanced,
            @Param("physicalDemandLevel") int physicalDemandLevel,
            @Param("goalStrength") boolean goalStrength,
            @Param("goalEndurance") boolean goalEndurance,
            @Param("goalFlexibility") boolean goalFlexibility,
            @Param("goalBalanceCoordination") boolean goalBalanceCoordination,
            @Param("goalMentalFocus") boolean goalMentalFocus
    );

    ApplicationUserPreferences findById(Long applicationUserId);
}
