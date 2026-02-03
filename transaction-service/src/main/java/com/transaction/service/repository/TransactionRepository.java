package com.transaction.service.repository;

import com.transaction.service.model.Transaction;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends ReactiveCrudRepository<Transaction, Long> {
    Mono<Transaction> findByTransactionId(String transactionId);

    Mono<Transaction> findByPaymentId(String paymentId);

    Flux<Transaction> findByAccountId(String accountId);

    Flux<Transaction> findByAccountIdAndStatusOrderByTransactionDateDesc(
            String accountId, String status);

    Flux<Transaction> findByAccountIdAndTransactionDateBetween(
            String accountId, LocalDateTime start, LocalDateTime end);

    Flux<Transaction> findByOrderId(String orderId);

    Flux<Transaction> findByGatewayTransactionId(String gatewayTransactionId);

    Flux<Transaction> findByReconciledFalse();

    @Query("SELECT * FROM transactions WHERE account_id = :accountId " +
            "AND transaction_date >= :startDate AND transaction_date <= :endDate " +
            "ORDER BY transaction_date DESC")
    Flux<Transaction> findAccountStatement(String accountId,
                                           LocalDateTime startDate,
                                           LocalDateTime endDate);

    @Query("SELECT COUNT(*) FROM transactions WHERE status = :status")
    Mono<Long> countByStatus(String status);

    @Query("SELECT SUM(amount) FROM transactions WHERE account_id = :accountId " +
            "AND transaction_category = :category AND status = 'COMPLETED'")
    Mono<Double> sumByAccountAndCategory(String accountId, String category);
}