package com.transaction.service.controller;

import com.transaction.service.dto.TransactionDto;
import com.transaction.service.dto.request.TransactionRequest;
import com.transaction.service.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // ─── CORE TRANSACTION OPERATIONS ────────────────────────────────

    @PostMapping
    public Mono<ResponseEntity<TransactionDto>> createTransaction(
            @RequestBody TransactionRequest request) {

        log.info("📝 Creating transaction for payment: {}", request.getPaymentId());

        return transactionService.createTransaction(request)
                .map(transaction -> ResponseEntity.ok(transaction))
                .onErrorResume(error -> {
                    log.error("❌ Failed to create transaction", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @GetMapping("/{transactionId}")
    public Mono<ResponseEntity<TransactionDto>> getTransaction(
            @PathVariable String transactionId) {

        log.info("🔍 Getting transaction: {}", transactionId);

        return transactionService.getTransaction(transactionId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    log.error("❌ Failed to get transaction", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    // ─── QUERY TRANSACTIONS ──────────────────────────────────────────

    @GetMapping("/account/{accountId}")
    public Flux<TransactionDto> getTransactionsByAccount(
            @PathVariable String accountId) {

        log.info("🔍 Getting transactions for account: {}", accountId);

        return transactionService.getTransactionsByAccount(accountId)
                .onErrorResume(error -> {
                    log.error("❌ Failed to get transactions for account", error);
                    return Flux.empty();
                });
    }
    @GetMapping("/payment/{paymentId}")
    public Mono<TransactionDto> getTransactionsByPayment(
            @PathVariable String paymentId) {

        log.info("Getting transactions for payment: {}", paymentId);

        return transactionService.getTransactionsByPayment(paymentId)
                .onErrorResume(error -> {
                    log.error("Failed to get transactions", error);
                    return Mono.empty();
                });
    }

    // ─── REVERSAL ────────────────────────────────────────────────────

    @PostMapping("/{transactionId}/reverse")
    public Mono<ResponseEntity<TransactionDto>> reverseTransaction(
            @PathVariable String transactionId,
            @RequestParam String reason) {

        log.info("🔄 Reversing transaction: {} reason: {}", transactionId, reason);

        return transactionService.reverseTransaction(transactionId, reason)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("❌ Failed to reverse transaction", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    // ─── RECONCILIATION ──────────────────────────────────────────────

    @GetMapping("/unreconciled")
    public Flux<TransactionDto> getUnreconciledTransactions() {
        log.info("🔍 Getting unreconciled transactions");

        return transactionService.getUnreconciledTransactions()
                .onErrorResume(error -> {
                    log.error("❌ Failed to get unreconciled transactions", error);
                    return Flux.empty();
                });
    }

    @PostMapping("/{transactionId}/reconcile")
    public Mono<ResponseEntity<Void>> markAsReconciled(
            @PathVariable String transactionId,
            @RequestParam String batchId) {

        log.info("✅ Marking transaction as reconciled: {} batchId: {}", transactionId, batchId);

        return transactionService.markAsReconciled(transactionId, batchId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(error -> {
                    log.error("❌ Failed to mark as reconciled", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}
