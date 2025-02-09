package com.inso.sila.repository;

import com.inso.sila.entity.Faqs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqsRepository extends JpaRepository<Faqs, Long> {
}
