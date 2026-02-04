package com.reconciliation.service.repository;


import com.reconciliation.service.model.ReconciliationBatch;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReconciliationBatchRepository extends ReactiveCrudRepository<ReconciliationBatch, Long> {

    Mono<ReconciliationBatch> findByBatchId(String batchId);

    Mono<ReconciliationBatch> findByProviderAndReconciliationDate(String provider, String date);

    Flux<ReconciliationBatch> findByStatusOrderByCreatedAtDesc(String status);

    Flux<ReconciliationBatch> findAllByOrderByCreatedAtDesc();
}