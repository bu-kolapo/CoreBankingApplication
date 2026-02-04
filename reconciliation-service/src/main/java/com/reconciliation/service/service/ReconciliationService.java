package com.reconciliation.service.service;

import com.reconciliation.service.dto.ReconciliationBatchDto;
import com.reconciliation.service.dto.request.ReconciliationRecordDto;
import com.reconciliation.service.dto.request.TriggerReconciliationRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReconciliationService {


    // Trigger a new reconciliation batch for a given date + provider
    Mono<ReconciliationBatchDto> triggerReconciliation(TriggerReconciliationRequest request);

    // Get a specific batch by ID
    Mono<ReconciliationBatchDto> getBatch(String batchId);

    // Get all batches (most recent first)
    Flux<ReconciliationBatchDto> getAllBatches();

    // Get all records inside a batch
    Flux<ReconciliationRecordDto> getBatchRecords(String batchId);

    // Get only mismatched/flagged records inside a batch
    Flux<ReconciliationRecordDto> getDiscrepancies(String batchId);

    // Get all records that need a human to look at
    Flux<ReconciliationRecordDto> getAllManualReviewItems();

    // Resolve a flagged record manually (admin marks it as reviewed)
    Mono<ReconciliationRecordDto> resolveManualReview(String recordId, String resolution);
}
