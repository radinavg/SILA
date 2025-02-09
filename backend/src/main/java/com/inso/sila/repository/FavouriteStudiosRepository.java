package com.inso.sila.repository;

import com.inso.sila.entity.Studio;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FavouriteStudiosRepository extends JpaRepository<Studio, Long> {


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM favourite_studios", nativeQuery = true)
    void deleteAll();
}
