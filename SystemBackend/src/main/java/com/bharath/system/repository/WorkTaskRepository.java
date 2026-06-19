package com.bharath.system.repository;
public interface WorkTaskRepository extends org.springframework.data.jpa.repository.JpaRepository<com.bharath.system.model.WorkTask, Long> {
    java.util.List<com.bharath.system.model.WorkTask> findByDate(java.time.LocalDate date);

    
}
