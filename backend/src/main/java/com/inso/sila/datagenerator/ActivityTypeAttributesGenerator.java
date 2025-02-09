package com.inso.sila.datagenerator;

import com.inso.sila.entity.ActivityTypeAttributes;
import com.inso.sila.entity.StudioActivityPreferences;
import com.inso.sila.enums.ActivityType;
import com.inso.sila.repository.ActivityTypeAttributesRepository;
import com.inso.sila.repository.StudioActivityPreferencesRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Profile("datagen")
@Component
public class ActivityTypeAttributesGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ActivityTypeAttributesRepository attributesRepository;
    private final StudioActivityPreferencesRepository preferencesRepository;

    public ActivityTypeAttributesGenerator(ActivityTypeAttributesRepository attributesRepository, StudioActivityPreferencesRepository preferencesRepository) {
        this.attributesRepository = attributesRepository;
        this.preferencesRepository = preferencesRepository;
    }

    @PostConstruct
    public void generateAttributes() {
        if (!attributesRepository.findAll().isEmpty()) {
            LOG.info("Attributes already generated");
        } else {
            LOG.info("Generating attributes...");
            generateData();
        }
    }

    private void generateData() {
        for (ActivityType activityType : ActivityType.values()) {
            StudioActivityPreferences preferences = createDefaultPreferencesForActivityType(activityType);
            preferencesRepository.save(preferences);

            ActivityTypeAttributes activityTypeAttributes = ActivityTypeAttributes.builder()
                    .activityType(activityType)
                    .attributes(preferences)
                    .build();

            attributesRepository.save(activityTypeAttributes);
            LOG.info("Generated attributes for activity type: {}", activityType);
        }
    }

    public StudioActivityPreferences createDefaultPreferencesForActivityType(ActivityType activityType) {
        StudioActivityPreferences preferences = new StudioActivityPreferences();

        switch (activityType) {
            case YOGA:
                preferences.setIndividual(true);
                preferences.setTeam(false);
                preferences.setWaterBased(false);
                preferences.setIndoor(true);
                preferences.setOutdoor(false);
                preferences.setBothIndoorAndOutdoor(false);
                preferences.setSuitableWarmClimate(true);
                preferences.setSuitableColdClimate(false);
                preferences.setRainCompatibility(true);
                preferences.setWindSuitability(true);
                preferences.setInvolvesUpperBody(true);
                preferences.setInvolvesLowerBody(true);
                preferences.setInvolvesCore(true);
                preferences.setInvolvesFullBody(true);
                preferences.setBeginner(false);
                preferences.setIntermediate(false);
                preferences.setAdvanced(false);
                preferences.setPhysicalDemandLevel(5);
                preferences.setGoalStrength(false);
                preferences.setGoalEndurance(false);
                preferences.setGoalFlexibility(true);
                preferences.setGoalBalanceCoordination(true);
                preferences.setGoalMentalFocus(true);
                break;

            case GROUP_ACTIVITIES:
                preferences.setIndividual(false);
                preferences.setTeam(true);
                preferences.setWaterBased(false);
                preferences.setIndoor(true);
                preferences.setOutdoor(false);
                preferences.setBothIndoorAndOutdoor(false);
                preferences.setSuitableWarmClimate(true);
                preferences.setSuitableColdClimate(true);
                preferences.setRainCompatibility(true);
                preferences.setWindSuitability(true);
                preferences.setInvolvesUpperBody(true);
                preferences.setInvolvesLowerBody(true);
                preferences.setInvolvesCore(true);
                preferences.setInvolvesFullBody(true);
                preferences.setBeginner(false);
                preferences.setIntermediate(true);
                preferences.setAdvanced(false);
                preferences.setPhysicalDemandLevel(5);
                preferences.setGoalStrength(false);
                preferences.setGoalEndurance(true);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(true);
                preferences.setGoalMentalFocus(false);
                break;

            case BALL_SPORTS:
                preferences.setIndividual(false);
                preferences.setTeam(true);
                preferences.setWaterBased(false);
                preferences.setIndoor(true);
                preferences.setOutdoor(false);
                preferences.setBothIndoorAndOutdoor(false);
                preferences.setSuitableWarmClimate(true);
                preferences.setSuitableColdClimate(true);
                preferences.setRainCompatibility(false);
                preferences.setWindSuitability(true);
                preferences.setInvolvesUpperBody(true);
                preferences.setInvolvesLowerBody(true);
                preferences.setInvolvesCore(true);
                preferences.setInvolvesFullBody(true);
                preferences.setBeginner(false);
                preferences.setIntermediate(true);
                preferences.setAdvanced(true);
                preferences.setPhysicalDemandLevel(7);
                preferences.setGoalStrength(true);
                preferences.setGoalEndurance(true);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(true);
                preferences.setGoalMentalFocus(false);
                break;

            case OUTDOOR_CLASSES:
                preferences.setIndividual(false);
                preferences.setTeam(true);
                preferences.setWaterBased(false);
                preferences.setIndoor(false);
                preferences.setOutdoor(true);
                preferences.setBothIndoorAndOutdoor(false);
                preferences.setSuitableWarmClimate(true);
                preferences.setSuitableColdClimate(false);
                preferences.setRainCompatibility(true);
                preferences.setWindSuitability(true);
                preferences.setInvolvesUpperBody(true);
                preferences.setInvolvesLowerBody(true);
                preferences.setInvolvesCore(true);
                preferences.setInvolvesFullBody(true);
                preferences.setBeginner(false);
                preferences.setIntermediate(true);
                preferences.setAdvanced(false);
                preferences.setPhysicalDemandLevel(6);
                preferences.setGoalStrength(true);
                preferences.setGoalEndurance(true);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(false);
                preferences.setGoalMentalFocus(false);
                break;

            case COMBAT_SPORTS:
                preferences.setIndividual(true);
                preferences.setTeam(false);
                preferences.setWaterBased(false);
                preferences.setIndoor(true);
                preferences.setOutdoor(false);
                preferences.setBothIndoorAndOutdoor(false);
                preferences.setSuitableWarmClimate(false);
                preferences.setSuitableColdClimate(true);
                preferences.setRainCompatibility(false);
                preferences.setWindSuitability(true);
                preferences.setInvolvesUpperBody(true);
                preferences.setInvolvesLowerBody(true);
                preferences.setInvolvesCore(true);
                preferences.setInvolvesFullBody(true);
                preferences.setBeginner(false);
                preferences.setIntermediate(true);
                preferences.setAdvanced(true);
                preferences.setPhysicalDemandLevel(8);
                preferences.setGoalStrength(true);
                preferences.setGoalEndurance(true);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(false);
                preferences.setGoalMentalFocus(true);
                break;

            case WATER_SPORTS:
                preferences.setIndividual(true);
                preferences.setTeam(false);
                preferences.setWaterBased(true);
                preferences.setIndoor(false);
                preferences.setOutdoor(true);
                preferences.setBothIndoorAndOutdoor(false);
                preferences.setSuitableWarmClimate(true);
                preferences.setSuitableColdClimate(false);
                preferences.setRainCompatibility(true);
                preferences.setWindSuitability(true);
                preferences.setInvolvesUpperBody(true);
                preferences.setInvolvesLowerBody(true);
                preferences.setInvolvesCore(true);
                preferences.setInvolvesFullBody(true);
                preferences.setBeginner(false);
                preferences.setIntermediate(true);
                preferences.setAdvanced(true);
                preferences.setPhysicalDemandLevel(7);
                preferences.setGoalStrength(true);
                preferences.setGoalEndurance(true);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(true);
                preferences.setGoalMentalFocus(false);
                break;

            case FITNESS_CLASSES:
                preferences.setIndividual(true);
                preferences.setTeam(false);
                preferences.setWaterBased(false);
                preferences.setIndoor(true);
                preferences.setOutdoor(false);
                preferences.setBothIndoorAndOutdoor(false);
                preferences.setSuitableWarmClimate(true);
                preferences.setSuitableColdClimate(false);
                preferences.setRainCompatibility(true);
                preferences.setWindSuitability(true);
                preferences.setInvolvesUpperBody(true);
                preferences.setInvolvesLowerBody(true);
                preferences.setInvolvesCore(true);
                preferences.setInvolvesFullBody(true);
                preferences.setBeginner(true);
                preferences.setIntermediate(true);
                preferences.setAdvanced(false);
                preferences.setPhysicalDemandLevel(4);
                preferences.setGoalStrength(true);
                preferences.setGoalEndurance(true);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(true);
                preferences.setGoalMentalFocus(false);
                break;

            default:
                preferences.setIndividual(false);
                preferences.setTeam(false);
                preferences.setWaterBased(false);
                preferences.setIndoor(true);
                preferences.setOutdoor(false);
                preferences.setBothIndoorAndOutdoor(false);
                preferences.setSuitableWarmClimate(false);
                preferences.setSuitableColdClimate(false);
                preferences.setRainCompatibility(false);
                preferences.setWindSuitability(false);
                preferences.setInvolvesUpperBody(false);
                preferences.setInvolvesLowerBody(false);
                preferences.setInvolvesCore(false);
                preferences.setInvolvesFullBody(false);
                preferences.setBeginner(false);
                preferences.setIntermediate(false);
                preferences.setAdvanced(false);
                preferences.setPhysicalDemandLevel(1);
                preferences.setGoalStrength(false);
                preferences.setGoalEndurance(false);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(false);
                preferences.setGoalMentalFocus(false);
                break;
        }

        return preferences;
    }
}
