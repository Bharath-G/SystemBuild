package com.bharath.system.repository;

import com.bharath.system.model.CompanionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanionReportRepository extends JpaRepository<CompanionReport, Long> {
}