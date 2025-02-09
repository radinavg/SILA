package com.inso.sila.repository;

import com.inso.sila.entity.StudioActivityPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface StudioActivityPreferencesRepository extends JpaRepository<StudioActivityPreferences, Integer> {


    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE StudioActivityPreferences u SET "
            + "u.isIndividual = :prefersIndividual, "
            + "u.isTeam = :prefersTeam, "
            + "u.isWaterBased = :prefersWaterBased, "
            + "u.isIndoor = :isIndoor, "
            + "u.isOutdoor = :isOutdoor, "
            + "u.isBothIndoorAndOutdoor = :isBothIndoorAndOutdoor, "
            + "u.suitableWarmClimate = :prefersWarmClimate, "
            + "u.suitableColdClimate = :prefersColdClimate, "
            + "u.rainCompatibility = :rainCompatibility, "
            + "u.windSuitability = :windSuitability, "
            + "u.involvesUpperBody = :focusUpperBody, "
            + "u.involvesLowerBody = :focusLowerBody, "
            + "u.involvesCore = :focusCore, "
            + "u.involvesFullBody = :focusFullBody, "
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
    void updateActivityPreferences(
            @Param("id") Long id,
            @Param("prefersIndividual") boolean prefersIndividual,
            @Param("prefersTeam") boolean prefersTeam,
            @Param("prefersWaterBased") boolean prefersWaterBased,
            @Param("isIndoor") boolean isIndoor,
            @Param("isOutdoor") boolean isOutdoor,
            @Param("isBothIndoorAndOutdoor") boolean isBothIndoorAndOutdoor,
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
}
