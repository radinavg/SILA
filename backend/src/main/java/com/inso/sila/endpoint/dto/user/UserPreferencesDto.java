package com.inso.sila.endpoint.dto.user;

public record UserPreferencesDto(
        boolean prefersIndividual,
        boolean prefersTeam,
        boolean prefersWaterBased,
        boolean prefersIndoor,
        boolean prefersOutdoor,
        boolean prefersBothIndoorAndOutdoor,
        boolean prefersWarmClimate,
        boolean prefersColdClimate,
        boolean rainCompatibility,
        boolean windSuitability,
        boolean focusUpperBody,
        boolean focusLowerBody,
        boolean focusCore,
        boolean focusFullBody,
        boolean isBeginner,
        boolean isIntermediate,
        boolean isAdvanced,
        int physicalDemandLevel, // 1-10
        boolean goalStrength,
        boolean goalEndurance,
        boolean goalFlexibility,
        boolean goalBalanceCoordination,
        boolean goalMentalFocus
){
}