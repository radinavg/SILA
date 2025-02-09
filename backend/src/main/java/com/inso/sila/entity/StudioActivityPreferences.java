package com.inso.sila.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "studio_activity_preferences")
@EqualsAndHashCode
public class StudioActivityPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_individual")
    private boolean isIndividual;

    @Column(name = "is_team")
    private boolean isTeam;

    @Column(name = "is_water_based")
    private boolean isWaterBased;

    @Column(name = "is_indoor")
    private boolean isIndoor;

    @Column(name = "is_outdoor")
    private boolean isOutdoor;

    @Column(name = "is_both_indoor_and_outdoor")
    private boolean isBothIndoorAndOutdoor;

    @Column(name = "suitable_warm_climate")
    private boolean suitableWarmClimate;

    @Column(name = "suitable_cold_climate")
    private boolean suitableColdClimate;

    @Column(name = "rain_compatibility")
    private boolean rainCompatibility;

    @Column(name = "wind_suitability")
    private boolean windSuitability;

    @Column(name = "involves_upper_body")
    private boolean involvesUpperBody;

    @Column(name = "involves_lower_body")
    private boolean involvesLowerBody;

    @Column(name = "involves_core")
    private boolean involvesCore;

    @Column(name = "involves_full_body")
    private boolean involvesFullBody;

    @Column(name = "is_beginner")
    private boolean isBeginner;

    @Column(name = "is_intermediate")
    private boolean isIntermediate;

    @Column(name = "is_advanced")
    private boolean isAdvanced;

    @Column(name = "physical_demand_level")
    private int physicalDemandLevel; // 1-10

    @Column(name = "goal_strength")
    private boolean goalStrength;

    @Column(name = "goal_endurance")
    private boolean goalEndurance;

    @Column(name = "goal_flexibility")
    private boolean goalFlexibility;

    @Column(name = "goal_balance_coordination")
    private boolean goalBalanceCoordination;

    @Column(name = "goal_mental_focus")
    private boolean goalMentalFocus;

}
