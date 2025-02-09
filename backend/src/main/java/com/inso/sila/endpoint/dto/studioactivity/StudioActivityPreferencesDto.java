package com.inso.sila.endpoint.dto.studioactivity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudioActivityPreferencesDto {
    private Long id;
    private Long studioActivityId;
    private boolean isIndividual;
    private boolean isTeam;
    private boolean isWaterBased;
    private boolean isIndoor;
    private boolean isOutdoor;
    private boolean isBothIndoorAndOutdoor;
    private boolean suitableWarmClimate;
    private boolean suitableColdClimate;
    private boolean rainCompatibility;
    private boolean windSuitability;
    private boolean focusUpperBody;
    private boolean focusLowerBody;
    private boolean focusCore;
    private boolean focusFullBody;
    private boolean isBeginner;
    private boolean isIntermediate;
    private boolean isAdvanced;
    private int physicalDemandLevel; // 1-10
    private boolean goalStrength;
    private boolean goalEndurance;
    private boolean goalFlexibility;
    private boolean goalBalanceCoordination;
    private boolean goalMentalFocus;
}