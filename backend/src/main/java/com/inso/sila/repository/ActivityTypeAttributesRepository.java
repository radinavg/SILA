package com.inso.sila.repository;

import com.inso.sila.entity.ActivityTypeAttributes;
import com.inso.sila.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityTypeAttributesRepository extends JpaRepository<ActivityTypeAttributes, Integer> {

    List<ActivityTypeAttributes> findByActivityType(ActivityType type);
}
