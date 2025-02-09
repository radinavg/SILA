package com.inso.sila.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Data
@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "recommendation_cluster")
public class RecommendationCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recommendationClusterId;

    @ManyToMany
    private Set<StudioActivity> recommendedActivities;
}
