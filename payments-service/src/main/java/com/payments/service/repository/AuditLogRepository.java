package com.payments.service.repository;

import com.payments.service.model.AuditLog;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends R2dbcRepository<AuditLog, Long> {
    Flux<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    Flux<AuditLog> findByChangedBy(String changedBy);

    @Query("SELECT * FROM audit_logs WHERE entity_type = :entityType " +
            "AND created_at BETWEEN :startDate AND :endDate ORDER BY created_at DESC")
    Flux<AuditLog> findAuditTrail(String entityType, LocalDateTime startDate, LocalDateTime endDate);
}