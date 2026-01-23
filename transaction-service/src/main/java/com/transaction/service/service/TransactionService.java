package com.transaction.service.service;

import com.transaction.service.dto.TransactionDto;
import com.transaction.service.dto.TransactionRequestDto;
import com.transaction.service.dto.TransactionResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {

    /**
     * Process a transaction (debit, credit, or transfer)
     */
    Mono<TransactionResponseDto> processTransaction(TransactionRequestDto request);

    /**
     * Get transaction by ID
     */
    Mono<TransactionDto> getTransactionById(String transactionId);

    /**
     * Get transaction history for an account
     */
    Flux<TransactionDto> getTransactionHistory(Long accountId, int limit);

    /**
     * Reverse a transaction
     */
    Mono<TransactionResponseDto> reverseTransaction(String transactionId, String reason);

}
