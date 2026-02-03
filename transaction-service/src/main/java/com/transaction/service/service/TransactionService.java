package com.transaction.service.service;

import com.transaction.service.dto.TransactionDto;
import com.transaction.service.dto.AccountStatementDto;
import com.transaction.service.dto.TransactionResponseDto;
import com.transaction.service.dto.request.TransactionRequest;
import com.transaction.service.events.PaymentCompletedEvent;
import com.transaction.service.events.RefundRequestEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {

    // Core transaction operations
    Mono<TransactionDto> createTransaction(TransactionRequest request);

    Mono<TransactionDto> getTransaction(String transactionId);

    // Returns raw transaction data only - no balance calculations
    Flux<TransactionDto> getTransactionsByAccount(String accountId);

    Mono<TransactionDto> getTransactionsByPayment(String paymentId);

    // Kafka event handlers
    Mono<TransactionDto> handlePaymentCompleted(PaymentCompletedEvent event);

    // Refund & Reversal
    Mono<TransactionDto> processRefund(RefundRequestEvent event);

    Mono<TransactionDto> reverseTransaction(String transactionId, String reason);

    // Reconciliation support
    Mono<Void> markAsReconciled(String transactionId, String batchId);

    Flux<TransactionDto> getUnreconciledTransactions();

}
