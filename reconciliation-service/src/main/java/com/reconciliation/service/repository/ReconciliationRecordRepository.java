package com.reconciliation.service.repository;

import com.reconciliation.service.model.ReconciliationRecord;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface ReconciliationRecordRepository extends R2dbcRepository<ReconciliationRecord, Long> {
    Mono<ReconciliationRecord> findByReconciliationId(String reconciliationId);
    Flux<ReconciliationRecord> findByPaymentId(Long paymentId);
    Flux<ReconciliationRecord> findByStatus(String status);

    @Query("SELECT * FROM reconciliation_records WHERE status = 'UNMATCHED' " +
            "OR status = 'DISCREPANCY' ORDER BY created_at DESC")
    Flux<ReconciliationRecord> findUnresolvedRecords();

    @Query("SELECT COUNT(*) FROM reconciliation_records WHERE status = :status " +
            "AND created_at BETWEEN :startDate AND :endDate")
    Mono<Long> countByStatusInDateRange(String status, LocalDateTime startDate, LocalDateTime endDate);
}
