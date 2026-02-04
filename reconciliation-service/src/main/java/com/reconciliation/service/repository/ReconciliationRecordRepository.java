package com.reconciliation.service.repository;

import com.reconciliation.service.model.ReconciliationBatch;
import com.reconciliation.service.model.ReconciliationRecord;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface ReconciliationRecordRepository extends ReactiveCrudRepository<ReconciliationRecord, Long> {
    Mono<ReconciliationRecord> findByRecordId(String recordId);

    Flux<ReconciliationRecord> findByBatchId(String batchId);

    Flux<ReconciliationRecord> findByBatchIdAndReconciliationStatus(String batchId, String status);

    Flux<ReconciliationRecord> findByRequiresManualReviewTrue();

    Mono<ReconciliationRecord> findByInternalTransactionId(String transactionId);

    Mono<ReconciliationRecord> findByGatewayTransactionId(String gatewayTransactionId);
}
