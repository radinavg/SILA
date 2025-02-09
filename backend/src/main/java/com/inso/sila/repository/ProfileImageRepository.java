package com.inso.sila.repository;

import com.inso.sila.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProfileImage i SET i.path = :path, i.name = :name WHERE i.profileImageId = :profileImageId")
    void updateProfileImage(@Param("profileImageId") Long profileImageId, @Param("path") String path, @Param("name") String name);
}
