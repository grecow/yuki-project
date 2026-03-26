package com.killiancorbel.realtimeapi.repositories;

import com.killiancorbel.realtimeapi.models.AuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    List<AuditLog> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);
    List<AuditLog> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after, Pageable pageable);
    long countByCreatedAtAfter(LocalDateTime after);
}
