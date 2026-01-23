package com.transaction.service.repository;

import com.transaction.service.model.Transaction;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends R2dbcRepository<Transaction, Long> {
    Mono<Transaction> findByTransactionId(String transactionId);
    Flux<Transaction> findBySourceAccountId(Long accountId);
    Flux<Transaction> findByDestinationAccountId(Long accountId);
    Flux<Transaction> findByStatus(String status);

    @Query("SELECT * FROM transactions WHERE source_account_id = :accountId " +
            "OR destination_account_id = :accountId ORDER BY transaction_date DESC LIMIT :limit")
    Flux<Transaction> findRecentTransactionsByAccountId(Long accountId, int limit);

    @Query("SELECT * FROM transactions WHERE transaction_date BETWEEN :startDate AND :endDate " +
            "AND status = :status")
    Flux<Transaction> findByDateRangeAndStatus(LocalDateTime startDate, LocalDateTime endDate, String status);

    @Query("SELECT SUM(amount) FROM transactions WHERE source_account_id = :accountId " +
            "AND transaction_date >= :since AND status = 'COMPLETED'")
    Mono<BigDecimal> calculateTotalDebitsSince(Long accountId, LocalDateTime since);
}