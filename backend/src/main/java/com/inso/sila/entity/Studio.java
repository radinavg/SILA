package com.inso.sila.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
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

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "studio")
public class Studio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studioId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(unique = true, nullable = false)
    private String location;

    @Column(nullable = false)
    @Builder.Default
    private float longitude = 0.0f;

    @Column(nullable = false)
    @Builder.Default
    private float latitude = 0.0f;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_image_id")
    @EqualsAndHashCode.Exclude
    private ProfileImage profileImage;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<GalleryImage> galleryImages;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Membership> memberships;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<StudioActivity> studioActivities;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Faqs> faqs;

    @Column
    private boolean approved;

    @ManyToMany(mappedBy = "favouriteStudios")
    private List<ApplicationUser> likedFromApplicationUsers;

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "studio_admin_id")
    private ApplicationUser studioAdmin;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Instructor> instructors;
}
