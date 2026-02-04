package com.reconciliation.service.service.implementation;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconciliation.service.client.PaymentGatewayClient;
import com.reconciliation.service.client.TransactionServiceClient;
import com.reconciliation.service.dto.GatewaySettlementRecord;
import com.reconciliation.service.dto.InternalTransactionRecord;
import com.reconciliation.service.dto.ReconciliationBatchDto;
import com.reconciliation.service.dto.request.ReconciliationRecordDto;
import com.reconciliation.service.dto.request.TriggerReconciliationRequest;
import com.reconciliation.service.model.ReconciliationBatch;
import com.reconciliation.service.model.ReconciliationRecord;
import com.reconciliation.service.repository.ReconciliationBatchRepository;
import com.reconciliation.service.repository.ReconciliationRecordRepository;
import com.reconciliation.service.service.ReconciliationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Slf4j
@Service
public class ReconciliationServiceImpl implements ReconciliationService {

    private final ReconciliationBatchRepository batchRepository;
    private final ReconciliationRecordRepository recordRepository;
    private final TransactionServiceClient transactionClient;
    private final PaymentGatewayClient gatewayClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ReconciliationServiceImpl(
            ReconciliationBatchRepository batchRepository,
            ReconciliationRecordRepository recordRepository,
            TransactionServiceClient transactionClient,
            PaymentGatewayClient gatewayClient,
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.batchRepository = batchRepository;
        this.recordRepository = recordRepository;
        this.transactionClient = transactionClient;
        this.gatewayClient = gatewayClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // ─── TRIGGER ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public Mono<ReconciliationBatchDto> triggerReconciliation(TriggerReconciliationRequest request) {

        log.info("🔄 Triggering reconciliation for {} on {}",
                request.getProvider(), request.getReconciliationDate());

        return batchRepository
                .findByProviderAndReconciliationDate(
                        request.getProvider(),
                        request.getReconciliationDate()
                )
                .flatMap(existing -> {
                    log.warn("⚠️ Reconciliation already exists for {} on {}",
                            request.getProvider(), request.getReconciliationDate());

                    return Mono.<ReconciliationBatchDto>error(
                            new RuntimeException("Reconciliation already exists for this provider and date")
                    );
                })
                .switchIfEmpty(Mono.defer(() -> createAndRunBatch(request)));
    }


    private Mono<ReconciliationBatchDto> createAndRunBatch(TriggerReconciliationRequest request) {
        String batchId = UUID.randomUUID().toString();
        String batchName = "RECON-" + request.getProvider() + "-" + request.getReconciliationDate();

        ReconciliationBatch batch = ReconciliationBatch.builder()
                .batchId(batchId)
                .batchName(batchName)
                .provider(request.getProvider())
                .status("INITIATED")
                .reconciliationDate(request.getReconciliationDate())
                .totalGatewayRecords(0L)
                .totalInternalRecords(0L)
                .matchedCount(0L)
                .unmatchedCount(0L)
                .missingInternalCount(0L)
                .missingInGatewayCount(0L)
                .startedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return batchRepository.save(batch)
                .flatMap(savedBatch -> runReconciliation(savedBatch, request));
    }

    private Mono<ReconciliationBatchDto> runReconciliation(ReconciliationBatch batch, TriggerReconciliationRequest request) {
        log.info("⚙️ Running reconciliation batch: {}", batch.getBatchId());

        batch.setStatus("PROCESSING");
        batch.setUpdatedAt(LocalDateTime.now());

        // Fetch BOTH sides in parallel: gateway records + internal records
        Mono<List<GatewaySettlementRecord>> gatewayRecords = gatewayClient
                .getStripeSettlements(request.getReconciliationDate())
                .collectList();

        Mono<List<InternalTransactionRecord>> internalRecords = transactionClient
                .getTransactionsByDate(request.getReconciliationDate())
                .collectList();

        return Mono.zip(gatewayRecords, internalRecords)
                .flatMap(tuple -> {
                    List<GatewaySettlementRecord> gateway = tuple.getT1();
                    List<InternalTransactionRecord> internal = tuple.getT2();

                    log.info("📊 Gateway records: {}, Internal records: {}", gateway.size(), internal.size());

                    // Compare and produce reconciliation records
                    List<ReconciliationRecord> records = compareRecords(batch.getBatchId(), gateway, internal);

                    // Count results
                    long matched = records.stream().filter(r -> "MATCHED".equals(r.getReconciliationStatus())).count();
                    long unmatched = records.stream().filter(r -> !"MATCHED".equals(r.getReconciliationStatus())).count();
                    long missingInternal = records.stream().filter(r -> "MISSING_INTERNAL".equals(r.getReconciliationStatus())).count();
                    long missingGateway = records.stream().filter(r -> "MISSING_IN_GATEWAY".equals(r.getReconciliationStatus())).count();

                    // Update batch totals
                    batch.setTotalGatewayRecords((long) gateway.size());
                    batch.setTotalInternalRecords((long) internal.size());
                    batch.setMatchedCount(matched);
                    batch.setUnmatchedCount(unmatched);
                    batch.setMissingInternalCount(missingInternal);
                    batch.setMissingInGatewayCount(missingGateway);
                    batch.setStatus("COMPLETED");
                    batch.setCompletedAt(LocalDateTime.now());
                    batch.setUpdatedAt(LocalDateTime.now());

                    // Save all reconciliation records
                    Flux<ReconciliationRecord> saveRecords = Flux.fromIterable(records)
                            .flatMap(recordRepository::save);

                    return saveRecords.then(batchRepository.save(batch))
                            .doOnSuccess(savedBatch -> {
                                log.info("✅ Reconciliation completed: {} matched={}, unmatched={}",
                                        savedBatch.getBatchId(), matched, unmatched);
                                publishReconciliationCompletedEvent(savedBatch);
                            })
                            .map(this::toBatchDto);
                })
                .onErrorResume(error -> {
                    log.error("❌ Reconciliation failed for batch: {}", batch.getBatchId(), error);
                    batch.setStatus("FAILED");
                    batch.setErrorMessage(error.getMessage());
                    batch.setUpdatedAt(LocalDateTime.now());
                    return batchRepository.save(batch).map(this::toBatchDto);
                });
    }

    // ─── CORE COMPARISON LOGIC ───────────────────────────────────────

    private List<ReconciliationRecord> compareRecords(
            String batchId,
            List<GatewaySettlementRecord> gatewayRecords,
            List<InternalTransactionRecord> internalRecords) {

        List<ReconciliationRecord> results = new ArrayList<>();

        // Build lookup maps for O(1) matching
        // Gateway keyed by its transactionId
        Map<String, GatewaySettlementRecord> gatewayMap = gatewayRecords.stream()
                .collect(Collectors.toMap(GatewaySettlementRecord::getTransactionId, r -> r, (a, b) -> a));

        // Internal keyed by the gatewayTransactionId we stored when the payment was made
        Map<String, InternalTransactionRecord> internalByGatewayId = internalRecords.stream()
                .collect(Collectors.toMap(InternalTransactionRecord::getGatewayTransactionId, r -> r, (a, b) -> a));

        Set<String> matchedGatewayIds = new HashSet<>();

        // ── Pass 1: Walk through internal records, try to find matching gateway record ──
        for (InternalTransactionRecord internal : internalRecords) {
            String gatewayId = internal.getGatewayTransactionId();

            if (gatewayId == null || gatewayId.isEmpty()) {
                // Internal record has no gateway ID — we can't match it
                results.add(buildRecord(batchId, internal, null, "MISSING_IN_GATEWAY",
                        "Internal record has no gateway transaction ID"));
                continue;
            }

            GatewaySettlementRecord gateway = gatewayMap.get(gatewayId);

            if (gateway == null) {
                // We have it internally but Stripe doesn't report it
                results.add(buildRecord(batchId, internal, null, "MISSING_IN_GATEWAY",
                        "Transaction exists internally but not in gateway settlements"));
            } else {
                matchedGatewayIds.add(gatewayId);
                // Both sides exist — now check if amounts and statuses match
                results.add(compareMatch(batchId, internal, gateway));
            }
        }

        // ── Pass 2: Walk through gateway records, find anything we DIDN'T match ──
        for (GatewaySettlementRecord gateway : gatewayRecords) {
            if (!matchedGatewayIds.contains(gateway.getTransactionId())) {
                // Stripe has this, but we have no internal record for it
                results.add(buildRecord(batchId, null, gateway, "MISSING_INTERNAL",
                        "Transaction exists in gateway but not in internal records"));
            }
        }

        return results;
    }

    private ReconciliationRecord compareMatch(
            String batchId,
            InternalTransactionRecord internal,
            GatewaySettlementRecord gateway) {

        // Check amount match
        boolean amountMatches = internal.getAmount().compareTo(gateway.getAmount()) == 0;

        // Check status match (normalize statuses to compare)
        boolean statusMatches = normalizeStatus(internal.getStatus())
                .equals(normalizeStatus(gateway.getStatus()));

        if (amountMatches && statusMatches) {
            return buildRecord(batchId, internal, gateway, "MATCHED", null);
        }

        // Build discrepancy details
        Map<String, String> discrepancy = new LinkedHashMap<>();
        if (!amountMatches) {
            discrepancy.put("type", "AMOUNT_MISMATCH");
            discrepancy.put("internalAmount", internal.getAmount().toPlainString());
            discrepancy.put("gatewayAmount", gateway.getAmount().toPlainString());
            discrepancy.put("difference", internal.getAmount().subtract(gateway.getAmount()).toPlainString());
        }
        if (!statusMatches) {
            discrepancy.put("type", discrepancy.containsKey("type") ? "AMOUNT_AND_STATUS_MISMATCH" : "STATUS_MISMATCH");
            discrepancy.put("internalStatus", internal.getStatus());
            discrepancy.put("gatewayStatus", gateway.getStatus());
        }

        String status = amountMatches ? "STATUS_MISMATCH" : (statusMatches ? "AMOUNT_MISMATCH" : "AMOUNT_AND_STATUS_MISMATCH");

        return buildRecord(batchId, internal, gateway, status, serializeToJson(discrepancy));
    }

    private ReconciliationRecord buildRecord(
            String batchId,
            InternalTransactionRecord internal,
            GatewaySettlementRecord gateway,
            String status,
            String details) {

        boolean needsReview = !"MATCHED".equals(status);

        return ReconciliationRecord.builder()
                .recordId(UUID.randomUUID().toString())
                .batchId(batchId)
                .internalTransactionId(internal != null ? internal.getTransactionId() : null)
                .internalPaymentId(internal != null ? internal.getPaymentId() : null)
                .internalAmount(internal != null ? internal.getAmount() : null)
                .internalStatus(internal != null ? internal.getStatus() : null)
                .gatewayTransactionId(gateway != null ? gateway.getTransactionId() : null)
                .gatewayAmount(gateway != null ? gateway.getAmount() : null)
                .gatewayStatus(gateway != null ? gateway.getStatus() : null)
                .gatewaySettledAt(gateway != null ? gateway.getSettledAt() : null)
                .reconciliationStatus(status)
                .discrepancyDetails(details)
                .requiresManualReview(needsReview)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Normalize "succeeded" ↔ "COMPLETED", "failed" ↔ "FAILED", etc.
    private String normalizeStatus(String status) {
        if (status == null) return "UNKNOWN";
        return switch (status.toUpperCase()) {
            case "SUCCEEDED", "COMPLETED", "SUCCESS" -> "COMPLETED";
            case "FAILED", "FAILURE" -> "FAILED";
            case "REFUNDED" -> "REFUNDED";
            case "PENDING", "PROCESSING" -> "PENDING";
            default -> status.toUpperCase();
        };
    }

    private String serializeToJson(Map<String, String> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return map.toString();
        }
    }

    // ─── QUERY METHODS ───────────────────────────────────────────────

    @Override
    public Mono<ReconciliationBatchDto> getBatch(String batchId) {
        return batchRepository.findByBatchId(batchId)
                .map(this::toBatchDto)
                .switchIfEmpty(Mono.error(new RuntimeException("Batch not found: " + batchId)));
    }

    @Override
    public Flux<ReconciliationBatchDto> getAllBatches() {
        return batchRepository.findAllByOrderByCreatedAtDesc()
                .map(this::toBatchDto);
    }

    @Override
    public Flux<ReconciliationRecordDto> getBatchRecords(String batchId) {
        return recordRepository.findByBatchId(batchId)
                .map(this::toRecordDto);
    }

    @Override
    public Flux<ReconciliationRecordDto> getDiscrepancies(String batchId) {
        return recordRepository.findByBatchIdAndReconciliationStatus(batchId, "MATCHED")
                .map(this::toRecordDto);
        // Note: this returns non-MATCHED. Adjust query if you want all non-MATCHED statuses.
    }

    @Override
    public Flux<ReconciliationRecordDto> getAllManualReviewItems() {
        return recordRepository.findByRequiresManualReviewTrue()
                .map(this::toRecordDto);
    }

    @Override
    @Transactional
    public Mono<ReconciliationRecordDto> resolveManualReview(String recordId, String resolution) {
        log.info("✅ Resolving manual review for record: {}", recordId);

        return recordRepository.findByRecordId(recordId)
                .switchIfEmpty(Mono.error(new RuntimeException("Record not found: " + recordId)))
                .flatMap(record -> {
                    record.setRequiresManualReview(false);
                    record.setDiscrepancyDetails(record.getDiscrepancyDetails() + " | RESOLVED: " + resolution);
                    record.setUpdatedAt(LocalDateTime.now());
                    return recordRepository.save(record);
                })
                .map(this::toRecordDto)
                .doOnSuccess(dto -> log.info("✅ Manual review resolved: {}", recordId));
    }

    // ─── KAFKA ───────────────────────────────────────────────────────

    private void publishReconciliationCompletedEvent(ReconciliationBatch batch) {
        try {
            kafkaTemplate.send("reconciliation-completed", batch.getBatchId(), toBatchDto(batch));
            log.info("📤 Published reconciliation completed event: {}", batch.getBatchId());
        } catch (Exception e) {
            log.error("❌ Failed to publish reconciliation event", e);
        }
    }

    // ─── MAPPERS ─────────────────────────────────────────────────────

    private ReconciliationBatchDto toBatchDto(ReconciliationBatch batch) {
        return ReconciliationBatchDto.builder()
                .batchId(batch.getBatchId())
                .batchName(batch.getBatchName())
                .provider(batch.getProvider())
                .status(batch.getStatus())
                .reconciliationDate(batch.getReconciliationDate())
                .totalGatewayRecords(batch.getTotalGatewayRecords())
                .totalInternalRecords(batch.getTotalInternalRecords())
                .matchedCount(batch.getMatchedCount())
                .unmatchedCount(batch.getUnmatchedCount())
                .missingInternalCount(batch.getMissingInternalCount())
                .missingInGatewayCount(batch.getMissingInGatewayCount())
                .startedAt(batch.getStartedAt())
                .completedAt(batch.getCompletedAt())
                .build();
    }

    private ReconciliationRecordDto toRecordDto(ReconciliationRecord record) {
        return ReconciliationRecordDto.builder()
                .recordId(record.getRecordId())
                .batchId(record.getBatchId())
                .internalTransactionId(record.getInternalTransactionId())
                .internalPaymentId(record.getInternalPaymentId())
                .internalAmount(record.getInternalAmount())
                .internalStatus(record.getInternalStatus())
                .gatewayTransactionId(record.getGatewayTransactionId())
                .gatewayAmount(record.getGatewayAmount())
                .gatewayStatus(record.getGatewayStatus())
                .reconciliationStatus(record.getReconciliationStatus())
                .discrepancyDetails(record.getDiscrepancyDetails())
                .requiresManualReview(record.getRequiresManualReview())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
