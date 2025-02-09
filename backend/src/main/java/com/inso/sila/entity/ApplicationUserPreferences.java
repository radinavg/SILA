package com.inso.sila.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "application_user_preferences")
public class ApplicationUserPreferences {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "application_user_id")
    @JsonBackReference
    private ApplicationUser user;


    @Column(name = "prefers_individual")
    private boolean prefersIndividual;

    @Column(name = "prefers_team")
    private boolean prefersTeam;

    @Column(name = "prefers_water_based")
    private boolean prefersWaterBased;

    @Column(name = "prefers_indoor")
    private boolean prefersIndoor;

    @Column(name = "prefers_outdoor")
    private boolean prefersOutdoor;

    @Column(name = "prefers_both_indoor_and_outdoor")
    private boolean prefersBothIndoorAndOutdoor;

    @Column(name = "prefers_warm_climate")
    private boolean prefersWarmClimate;

    @Column(name = "prefers_cold_climate")
    private boolean prefersColdClimate;

    @Column(name = "rain_compatibility")
    private boolean rainCompatibility;

    @Column(name = "wind_suitability")
    private boolean windSuitability;

    @Column(name = "focus_upper_body")
    private boolean focusUpperBody;

    @Column(name = "focus_lower_body")
    private boolean focusLowerBody;

    @Column(name = "focus_core")
    private boolean focusCore;

    @Column(name = "focus_full_body")
    private boolean focusFullBody;

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
