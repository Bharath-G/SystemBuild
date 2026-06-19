package com.bharath.system.repository;

import com.bharath.system.model.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuestRepository extends JpaRepository<Quest, Long> {
    List<Quest> findByCompletedFalseOrderByDeadlineAsc();
    List<Quest> findByCompletedTrueOrderByCompletedAtDesc();
    List<Quest> findByTypeAndCreatedAtAfter(String type, LocalDateTime after);
    long countByCompletedTrue();
    long countByCategoryAndCompletedTrue(String category);
}

