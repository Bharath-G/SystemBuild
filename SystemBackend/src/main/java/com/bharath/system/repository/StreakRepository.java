package com.bharath.system.repository;

import com.bharath.system.model.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StreakRepository extends JpaRepository<Streak, Long> {
    List<Streak> findAllByOrderByTypeAsc();
    Optional<Streak> findByType(String type);
}
