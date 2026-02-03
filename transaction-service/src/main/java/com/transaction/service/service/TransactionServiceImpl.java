package com.transaction.service.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.transaction.service.dto.TransactionDto;
import com.transaction.service.dto.request.TransactionRequest;
import com.transaction.service.events.PaymentCompletedEvent;
import com.transaction.service.events.RefundRequestEvent;
import com.transaction.service.model.LedgerEntry;
import com.transaction.service.model.Transaction;
import com.transaction.service.repository.LedgerEntryRepository;
import com.transaction.service.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            LedgerEntryRepository ledgerEntryRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Mono<TransactionDto> createTransaction(TransactionRequest request) {
        log.info("💰 Creating transaction for payment: {}", request.getPaymentId());

        String transactionId = UUID.randomUUID().toString();
        String referenceNumber = generateReferenceNumber();

        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .paymentId(request.getPaymentId())
                .orderId(request.getOrderId())
                .accountId(request.getAccountId())
                .transactionType(request.getTransactionType())
                .transactionCategory("DEBIT") // Customer pays
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status("PENDING")
                .description(request.getDescription())
                .gatewayTransactionId(request.getGatewayTransactionId())
                .paymentGateway(request.getPaymentGateway())
                .referenceNumber(referenceNumber)
                .reconciled(false)
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction)
                .doOnSuccess(saved -> log.info("💾 Transaction created: {}", saved.getTransactionId()))
                .map(this::toDto);
    }

    @Override
    @Transactional
    public Mono<TransactionDto> handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("✅ Handling payment completed event: {}", event.getPaymentId());

        // Check if transaction already exists for this payment
        return transactionRepository.findByPaymentId(event.getPaymentId())
                .flatMap(existing -> {
                    log.info("♻️ Transaction already exists for payment: {}", event.getPaymentId());

                    // Update status if needed
                    if ("PENDING".equals(existing.getStatus())) {
                        return completeTransaction(existing, event);
                    }
                    return Mono.just(existing);
                })
                .switchIfEmpty(Mono.defer(() -> createTransactionFromPayment(event)))
                .map(this::toDto)
                .doOnSuccess(dto -> publishTransactionCompletedEvent(dto));
    }

    private Mono<Transaction> createTransactionFromPayment(PaymentCompletedEvent event) {
        log.info("🆕 Creating new transaction from payment event: {}", event.getPaymentId());

        String transactionId = UUID.randomUUID().toString();
        String referenceNumber = generateReferenceNumber();

        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .paymentId(event.getPaymentId())
                .orderId(event.getOrderId())
                .accountId(event.getAccountId())
                .transactionType("PAYMENT")
                .transactionCategory("DEBIT")
                .amount(new BigDecimal(event.getAmount()))
                .currency(event.getCurrency())
                .status("COMPLETED")
                .description("Payment for order " + event.getOrderId())
                .gatewayTransactionId(event.getGatewayTransactionId())
                .paymentGateway(event.getPaymentGateway())
                .referenceNumber(referenceNumber)
                .reconciled(false)
                .transactionDate(LocalDateTime.now())
                .settledAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction)
                .flatMap(saved -> createLedgerEntries(saved))
                .doOnSuccess(saved -> log.info("✅ Transaction completed: {}", saved.getTransactionId()));
    }

    private Mono<Transaction> completeTransaction(Transaction transaction, PaymentCompletedEvent event) {
        log.info("🔄 Completing existing transaction: {}", transaction.getTransactionId());

        transaction.setStatus("COMPLETED");
        transaction.setGatewayTransactionId(event.getGatewayTransactionId());
        transaction.setSettledAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        return transactionRepository.save(transaction)
                .flatMap(saved -> createLedgerEntries(saved));
    }

    @Transactional
    private Mono<Transaction> createLedgerEntries(Transaction transaction) {
        log.info("📒 Creating ledger entries for transaction: {}", transaction.getTransactionId());

        // Debit Entry - Customer Account (money out)
        LedgerEntry debitEntry = LedgerEntry.builder()
                .entryId(UUID.randomUUID().toString())
                .transactionId(transaction.getTransactionId())
                .accountId(transaction.getAccountId())
                .accountType("CUSTOMER")
                .entryType("DEBIT")
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .referenceNumber(transaction.getReferenceNumber())
                .entryDate(transaction.getTransactionDate())
                .createdAt(LocalDateTime.now())
                .build();

        // Credit Entry - Merchant/Gateway Account (money in)
        LedgerEntry creditEntry = LedgerEntry.builder()
                .entryId(UUID.randomUUID().toString())
                .transactionId(transaction.getTransactionId())
                .accountId("MERCHANT-001") // Your merchant account
                .accountType("MERCHANT")
                .entryType("CREDIT")
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .referenceNumber(transaction.getReferenceNumber())
                .entryDate(transaction.getTransactionDate())
                .createdAt(LocalDateTime.now())
                .build();

        // Save both entries (double-entry bookkeeping)
        return ledgerEntryRepository.save(debitEntry)
                .then(ledgerEntryRepository.save(creditEntry))
                .thenReturn(transaction)
                .doOnSuccess(t -> log.info("📒 Ledger entries created for: {}", t.getTransactionId()));
    }

    @Override
    @Transactional
    public Mono<TransactionDto> processRefund(RefundRequestEvent event) {
        log.info("💸 Processing refund for payment: {}", event.getPaymentId());

        return transactionRepository.findByPaymentId(event.getPaymentId())
                .switchIfEmpty(Mono.error(new RuntimeException("Original transaction not found")))
                .flatMap(originalTransaction -> {

                    String refundTransactionId = UUID.randomUUID().toString();
                    String referenceNumber = generateReferenceNumber();

                    Transaction refundTransaction = Transaction.builder()
                            .transactionId(refundTransactionId)
                            .paymentId(event.getPaymentId())
                            .orderId(event.getOrderId())
                            .accountId(event.getAccountId())
                            .transactionType("REFUND")
                            .transactionCategory("CREDIT") // Customer receives money back
                            .amount(new BigDecimal(event.getAmount()))
                            .currency(event.getCurrency())
                            .status("COMPLETED")
                            .description("Refund: " + event.getReason())
                            .gatewayTransactionId(originalTransaction.getGatewayTransactionId())
                            .paymentGateway(originalTransaction.getPaymentGateway())
                            .referenceNumber(referenceNumber)
                            .reconciled(false)
                            .transactionDate(LocalDateTime.now())
                            .settledAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return transactionRepository.save(refundTransaction)
                            .flatMap(this::createRefundLedgerEntries);
                })
                .map(this::toDto)
                .doOnSuccess(dto -> {
                    log.info("✅ Refund processed: {}", dto.getTransactionId());
                    publishRefundCompletedEvent(dto);
                });
    }

    private Mono<Transaction> createRefundLedgerEntries(Transaction refundTransaction) {
        log.info("📒 Creating refund ledger entries: {}", refundTransaction.getTransactionId());

        // Credit Entry - Customer Account (money in)
        LedgerEntry creditEntry = LedgerEntry.builder()
                .entryId(UUID.randomUUID().toString())
                .transactionId(refundTransaction.getTransactionId())
                .accountId(refundTransaction.getAccountId())
                .accountType("CUSTOMER")
                .entryType("CREDIT")
                .amount(refundTransaction.getAmount())
                .currency(refundTransaction.getCurrency())
                .description(refundTransaction.getDescription())
                .referenceNumber(refundTransaction.getReferenceNumber())
                .entryDate(refundTransaction.getTransactionDate())
                .createdAt(LocalDateTime.now())
                .build();

        // Debit Entry - Merchant Account (money out)
        LedgerEntry debitEntry = LedgerEntry.builder()
                .entryId(UUID.randomUUID().toString())
                .transactionId(refundTransaction.getTransactionId())
                .accountId("MERCHANT-001")
                .accountType("MERCHANT")
                .entryType("DEBIT")
                .amount(refundTransaction.getAmount())
                .currency(refundTransaction.getCurrency())
                .description(refundTransaction.getDescription())
                .referenceNumber(refundTransaction.getReferenceNumber())
                .entryDate(refundTransaction.getTransactionDate())
                .createdAt(LocalDateTime.now())
                .build();

        return ledgerEntryRepository.save(creditEntry)
                .then(ledgerEntryRepository.save(debitEntry))
                .thenReturn(refundTransaction);
    }

    @Override
    @Transactional
    public Mono<TransactionDto> reverseTransaction(String transactionId, String reason) {
        log.info("🔄 Reversing transaction: {}", transactionId);

        return transactionRepository.findByTransactionId(transactionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found")))
                .flatMap(originalTransaction -> {

                    // Update original transaction status
                    originalTransaction.setStatus("REVERSED");
                    originalTransaction.setUpdatedAt(LocalDateTime.now());

                    String reversalId = UUID.randomUUID().toString();
                    String referenceNumber = generateReferenceNumber();

                    // Create reversal transaction
                    Transaction reversalTransaction = Transaction.builder()
                            .transactionId(reversalId)
                            .paymentId(originalTransaction.getPaymentId())
                            .orderId(originalTransaction.getOrderId())
                            .accountId(originalTransaction.getAccountId())
                            .transactionType("REVERSAL")
                            .transactionCategory(
                                    "DEBIT".equals(originalTransaction.getTransactionCategory())
                                            ? "CREDIT" : "DEBIT"
                            )
                            .amount(originalTransaction.getAmount())
                            .currency(originalTransaction.getCurrency())
                            .status("COMPLETED")
                            .description("Reversal: " + reason)
                            .referenceNumber(referenceNumber)
                            .reconciled(false)
                            .transactionDate(LocalDateTime.now())
                            .settledAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return transactionRepository.save(originalTransaction)
                            .then(transactionRepository.save(reversalTransaction))
                            .flatMap(this::createReversalLedgerEntries);
                })
                .map(this::toDto)
                .doOnSuccess(dto -> log.info("✅ Transaction reversed: {}", dto.getTransactionId()));
    }

    private Mono<Transaction> createReversalLedgerEntries(Transaction reversalTransaction) {
        // Similar to refund ledger entries
        // This would create opposite entries to reverse the effect
        return Mono.just(reversalTransaction);
    }

    @Override
    public Mono<TransactionDto> getTransaction(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .map(this::toDto);
    }

    @Override
    public Flux<TransactionDto> getTransactionsByAccount(String accountId) {
        return transactionRepository.findByAccountId(accountId)
                .map(this::toDto);
    }

    @Override
    public Mono<TransactionDto> getTransactionsByPayment(String paymentId) {
        return transactionRepository.findByPaymentId(paymentId)
                .map(this::toDto);
    }


    @Override
    public Mono<Void> markAsReconciled(String transactionId, String batchId) {
        return transactionRepository.findByTransactionId(transactionId)
                .flatMap(transaction -> {
                    transaction.setReconciled(true);
                    transaction.setReconciledAt(LocalDateTime.now());
                    transaction.setReconciliationBatchId(batchId);
                    transaction.setUpdatedAt(LocalDateTime.now());

                    return transactionRepository.save(transaction);
                })
                .then()
                .doOnSuccess(v -> log.info("✅ Transaction marked as reconciled: {}", transactionId));
    }

    @Override
    public Flux<TransactionDto> getUnreconciledTransactions() {
        return transactionRepository.findByReconciledFalse()
                .map(this::toDto);
    }

    // Helper methods
    private String generateReferenceNumber() {
        return "TXN-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void publishTransactionCompletedEvent(TransactionDto transaction) {
        try {
            kafkaTemplate.send("transaction-completed", transaction.getTransactionId(), transaction);
            log.info("📤 Published transaction completed event: {}", transaction.getTransactionId());
        } catch (Exception e) {
            log.error("❌ Failed to publish transaction completed event", e);
        }
    }

    private void publishRefundCompletedEvent(TransactionDto transaction) {
        try {
            kafkaTemplate.send("refund-completed", transaction.getTransactionId(), transaction);
            log.info("📤 Published refund completed event: {}", transaction.getTransactionId());
        } catch (Exception e) {
            log.error("❌ Failed to publish refund completed event", e);
        }
    }

    private TransactionDto toDto(Transaction transaction) {
        return TransactionDto.builder()
                .transactionId(transaction.getTransactionId())
                .paymentId(transaction.getPaymentId())
                .orderId(transaction.getOrderId())
                .accountId(transaction.getAccountId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .gatewayTransactionId(transaction.getGatewayTransactionId())
                .referenceNumber(transaction.getReferenceNumber())
                .transactionDate(transaction.getTransactionDate())
                .build();
    }
}