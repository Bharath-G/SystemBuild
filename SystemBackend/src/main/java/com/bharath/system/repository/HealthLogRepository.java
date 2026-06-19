package com.bharath.system.repository;

import com.bharath.system.model.HealthLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthLogRepository extends JpaRepository<HealthLog, Long> {
    Optional<HealthLog> findByDate(LocalDate date);
    List<HealthLog> findByDateBetweenOrderByDateDesc(LocalDate start, LocalDate end);
}
