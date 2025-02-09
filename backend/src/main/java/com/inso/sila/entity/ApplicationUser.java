package com.inso.sila.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.inso.sila.enums.Gender;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "application_user")
public class ApplicationUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationUserId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "gender")
    @Enumerated(EnumType.ORDINAL)
    private Gender gender;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    @Builder.Default
    private float longitude = 0.0f;

    @Column(nullable = false)
    @Builder.Default
    private float latitude = 0.0f;

    @ManyToMany
    @JoinTable(
            name = "user_studio_activities",
            joinColumns = @JoinColumn(name = "application_user_id"),
            inverseJoinColumns = @JoinColumn(name = "studio_activity_id")
    )
    @JsonManagedReference
    private List<StudioActivity> studioActivities;


    @ManyToMany
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "application_user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Set<ApplicationUser> friends;

    @ManyToMany(mappedBy = "applicationUsers")
    @JsonBackReference
    private Set<Membership> memberships;

    @ManyToMany
    @JoinTable(
            name = "favourite_studios",
            joinColumns = @JoinColumn(name = "application_user_id"),
            inverseJoinColumns = @JoinColumn(name = "studio_id"))
    private List<Studio> favouriteStudios;


    @Column(name = "login_attempts", nullable = false)
    private int loginAttempts;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    @Column(name = "is_locked", nullable = false)
    private boolean isLocked;

    @Column(name = "is_studio_admin", nullable = false)
    private boolean isStudioAdmin;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_image_id")
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    private ProfileImage profileImage;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    private ApplicationUserPreferences preferences;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_cluster_id")
    private RecommendationCluster recommendationCluster;

    @Column
    private boolean preferencesSet;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<FriendRequest> receivedFriendRequests;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ActivityInvitation> receivedActivityInvitations;

    @ManyToMany
    @JoinTable(
            name = "user_studio_recommendations",
            joinColumns = @JoinColumn(name = "application_user_id"),
            inverseJoinColumns = @JoinColumn(name = "studio_id")
    )
    @JsonManagedReference
    private List<Studio> studioRecommendations;

}