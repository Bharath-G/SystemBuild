package com.bharath.system.repository;

import com.bharath.system.model.CompanionMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompanionMemoryRepository extends JpaRepository<CompanionMemory, Long> {
    List<CompanionMemory> findByDate(LocalDate date);
}