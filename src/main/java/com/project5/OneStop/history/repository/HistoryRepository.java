package com.project5.OneStop.history.repository;

import com.project5.OneStop.history.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoryRepository
        extends JpaRepository<History, Long> {

    List<History> findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId,
            LocalDateTime now
    );

    void deleteByExpiresAtBefore(
            LocalDateTime now
    );
}