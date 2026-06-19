package com.bharath.system.repository;

import com.bharath.system.model.DailyInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyInsightRepository extends JpaRepository<DailyInsight, Long> {
    Optional<DailyInsight> findByDate(LocalDate date);
}
