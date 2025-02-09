package com.inso.sila.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "profile_image")
public class ProfileImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileImageId;

    @Column(name = "name")
    private String name;

    @Column(name = "path")
    private String path;

    @OneToOne(mappedBy = "profileImage")
    private Studio studio;

    @OneToOne(mappedBy = "profileImage")
    private StudioActivity studioActivity;

    @OneToOne(mappedBy = "profileImage")
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    private ApplicationUser applicationUser;
}
