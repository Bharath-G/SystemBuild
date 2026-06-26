package com.bharath.system.repository;

import com.bharath.system.model.FinancialInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialInsightRepository extends JpaRepository<FinancialInsight, Long> {
    List<FinancialInsight> findAllByOrderByGeneratedDateDesc();
}
