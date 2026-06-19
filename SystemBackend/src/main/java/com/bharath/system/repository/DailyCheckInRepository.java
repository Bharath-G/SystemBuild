package com.bharath.system.repository;

import com.bharath.system.model.DailyCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyCheckInRepository extends JpaRepository<DailyCheckIn, Long> {
    Optional<DailyCheckIn> findByDate(LocalDate date);
}
