package com.reconciliation.service.controller;

import com.reconciliation.service.dto.ReconciliationBatchDto;
import com.reconciliation.service.dto.request.ReconciliationRecordDto;
import com.reconciliation.service.dto.request.TriggerReconciliationRequest;
import com.reconciliation.service.service.ReconciliationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    // ─── TRIGGER ─────────────────────────────────────────────────────

    @PostMapping("/trigger")
    public Mono<ResponseEntity<ReconciliationBatchDto>> triggerReconciliation(
            @Valid @RequestBody TriggerReconciliationRequest request) {

        log.info("🔄 Trigger reconciliation request: {} for {}", request.getProvider(), request.getReconciliationDate());

        return reconciliationService.triggerReconciliation(request)
                .map(batch -> ResponseEntity.status(HttpStatus.CREATED).body(batch))
                .onErrorResume(error -> {
                    log.error("❌ Failed to trigger reconciliation", error);
                    return Mono.just(ResponseEntity.badRequest().<ReconciliationBatchDto>build());
                });
    }

    // ─── BATCHES ─────────────────────────────────────────────────────

    @GetMapping("/batches")
    public Flux<ReconciliationBatchDto> getAllBatches() {
        log.info("🔍 Fetching all reconciliation batches");
        return reconciliationService.getAllBatches();
    }

    @GetMapping("/batches/{batchId}")
    public Mono<ResponseEntity<ReconciliationBatchDto>> getBatch(@PathVariable String batchId) {
        log.info("🔍 Fetching batch: {}", batchId);

        return reconciliationService.getBatch(batchId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    log.error("❌ Failed to get batch", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    // ─── RECORDS ─────────────────────────────────────────────────────

    @GetMapping("/batches/{batchId}/records")
    public Flux<ReconciliationRecordDto> getBatchRecords(@PathVariable String batchId) {
        log.info("🔍 Fetching all records for batch: {}", batchId);
        return reconciliationService.getBatchRecords(batchId);
    }

    @GetMapping("/batches/{batchId}/discrepancies")
    public Flux<ReconciliationRecordDto> getDiscrepancies(@PathVariable String batchId) {
        log.info("🔍 Fetching discrepancies for batch: {}", batchId);
        return reconciliationService.getDiscrepancies(batchId);
    }

    // ─── MANUAL REVIEW ───────────────────────────────────────────────

    @GetMapping("/manual-review")
    public Flux<ReconciliationRecordDto> getAllManualReviewItems() {
        log.info("🔍 Fetching all items pending manual review");
        return reconciliationService.getAllManualReviewItems();
    }

    @PostMapping("/manual-review/{recordId}/resolve")
    public Mono<ResponseEntity<ReconciliationRecordDto>> resolveManualReview(
            @PathVariable String recordId,
            @RequestParam String resolution) {

        log.info("✅ Resolving manual review: {} resolution: {}", recordId, resolution);

        return reconciliationService.resolveManualReview(recordId, resolution)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("❌ Failed to resolve manual review", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}