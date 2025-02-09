package com.inso.sila.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@Table(name = "membership")
public class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long membershipId;

    @Column(nullable = false)
    private String name;

    @Column
    private Integer duration;

    @Column
    private Integer minDuration;

    @Column
    private Float price;

    @ManyToOne(fetch = FetchType.LAZY)
    private Studio studio;

    @ManyToMany
    @JoinTable(
            name = "user_membership",
            joinColumns = @JoinColumn(name = "membership_id"),
            inverseJoinColumns = @JoinColumn(name = "application_user_id")
    )
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @JsonManagedReference
    private Set<ApplicationUser> applicationUsers;

    @OneToOne(mappedBy = "membership", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "studio_activity_id")
    private StudioActivity studioActivity;


}
