package com.reconciliation.service.service;

import com.reconciliation.service.dto.request.ReconciliationRecordDto;
import com.reconciliation.service.dto.response.ReconciliationResponseDto;
import com.reconciliation.service.dto.request.ReconciliationRequestDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReconcilePayment {
    /**
     * Reconcile a specific payment
     */
    Mono<Void> reconcilePayment(Long paymentId);

    /**
     * Run bulk reconciliation
     */
    Mono<ReconciliationResponseDto> runReconciliation(ReconciliationRequestDto request);

    /**
     * Get unresolved reconciliation records
     */
    Flux<ReconciliationRecordDto> getUnresolvedRecords();

    /**
     * Resolve discrepancy
     */
    Mono<Void> resolveDiscrepancy(String reconciliationId, String resolution);
}

