package com.bharath.system.repository;

import com.bharath.system.model.GoogleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoogleConfigRepository extends JpaRepository<GoogleConfig, Long> {
}
