package com.inso.sila.repository;

import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.enums.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Integer> {

    ApplicationUser findByEmail(String email);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ApplicationUser u SET u.loginAttempts = :loginAttempts WHERE u.email = :email")
    void setLoginAttempts(@Param("email") String email, @Param("loginAttempts") int loginAttempts);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ApplicationUser u SET u.isLocked = :isLocked WHERE u.email = :email")
    void updateIsLocked(@Param("email") String email, @Param("isLocked") Boolean isLocked);

    @Transactional
    @Modifying
    @Query("UPDATE ApplicationUser u SET u.password = :password WHERE u.email = :email")
    void updatePasswordOnEmail(@Param("email") String email, @Param("password") String password);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ApplicationUser u SET u.firstName = :firstName, "
            + "u.lastName = :lastName, u.location = :location, u.longitude = :longitude, u.latitude = :latitude,"
            + " u.gender = :gender WHERE u.email = :email")
    void updateApplicationUser(@Param("email") String email, @Param("firstName") String firstName, @Param("lastName") String lastName,
                               @Param("location") String location, @Param("longitude") float longitude, @Param("latitude") float latitude,
                               @Param("gender") Gender gender);

    @Query("SELECT u FROM ApplicationUser u WHERE "
            + "(:isAdmin IS NULL OR :isAdmin = u.isAdmin) "
            + "AND (:isLocked IS NULL OR :isLocked = u.isLocked)")
    List<ApplicationUser> findAdminsOrLockedAdmins(@Param("isAdmin") Boolean isAdmin,
                                                   @Param("isLocked") Boolean isLocked);


    @Query("SELECT u FROM ApplicationUser u WHERE "
            + "(LOWER(u.firstName) LIKE CONCAT('%', LOWER(:firstName), '%') OR :firstName IS NULL) "
            + "AND (LOWER(u.lastName) LIKE CONCAT('%', LOWER(:lastName), '%') OR :lastName IS NULL) "
            + "AND (LOWER(u.email) LIKE CONCAT('%', LOWER(:email), '%') OR :email IS NULL) "
            + "AND (:isAdmin IS NULL OR :isAdmin = u.isAdmin) "
            + "AND (:isLocked IS NULL OR :isLocked = u.isLocked) "
            + "ORDER BY "
            + "CASE "
            + "  WHEN u.isAdmin = true THEN 0 "
            + "  WHEN u.isStudioAdmin = true THEN 1 "
            + "  ELSE 2 "
            + "END, "
            + "u.firstName ASC, "
            + "u.lastName ASC"
    )
    Page<ApplicationUser> findBySearch(@Param("firstName") String firstName,
                                       @Param("lastName") String lastName,
                                       @Param("email") String email,
                                       @Param("isAdmin") Boolean isAdmin,
                                       @Param("isLocked") Boolean isLocked,
                                       Pageable pageable);


    @Query("SELECT u FROM ApplicationUser u LEFT JOIN FETCH u.profileImage WHERE "
            + "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) OR :firstName IS NULL) "
            + "AND (LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')) OR :lastName IS NULL) "
            + "AND (LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')) OR :email IS NULL) "
            + "AND u.isAdmin = false "
            + "AND u.isStudioAdmin = false")
    List<ApplicationUser> searchUsers(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            Pageable pageable
    );




    @Query("SELECT f FROM ApplicationUser u JOIN u.friends f LEFT JOIN FETCH f.profileImage "
           + "WHERE u.email = :myEmail "
           + "AND (:firstName IS NULL OR :firstName = '' OR LOWER(f.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) "
           + "AND (:lastName IS NULL OR :lastName = '' OR LOWER(f.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) "
           + "AND (:email IS NULL OR :email = '' OR LOWER(f.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    List<ApplicationUser> searchMyFriends(
            @Param("myEmail") String myEmail,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            Pageable pageable
    );

    @Query("SELECT u FROM ApplicationUser u ORDER BY RANDOM() LIMIT 1")
    ApplicationUser findRandomUser();

    @Query("SELECT u FROM ApplicationUser u WHERE u.isStudioAdmin = false AND u.isLocked = false AND u.isAdmin = false")
    List<ApplicationUser> findUsersThatAreNotStudioAdminsOrBlocked();
}
