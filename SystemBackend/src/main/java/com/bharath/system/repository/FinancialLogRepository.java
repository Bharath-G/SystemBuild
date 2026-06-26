package com.bharath.system.repository;

import com.bharath.system.model.FinancialLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialLogRepository extends JpaRepository<FinancialLog, Long> {
    
    @Query("SELECT f FROM FinancialLog f WHERE f.date >= :startDate AND f.date <= :endDate ORDER BY f.date DESC")
    List<FinancialLog> findByDateBetweenOrderByDateDesc(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
}
