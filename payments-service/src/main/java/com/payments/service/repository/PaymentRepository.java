package com.payments.service.repository;

// payments-app/src/main/java/com.bank.payments/repo/PaymentRepository.java
import com.payments.service.model.Payment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface PaymentRepository extends ReactiveCrudRepository<Payment, Long> {
    Mono<Payment> findByPaymentId(String paymentId);
    Mono<Payment> findByGatewayTransactionId(String gatewayTransactionId);
    Flux<Payment> findByOrderId(Long orderId);
    Flux<Payment> findByAccountId(Long accountId);
    Flux<Payment> findByStatus(String status);

    @Query("SELECT * FROM payments WHERE payment_gateway = :gateway AND status = :status " +
            "AND created_at BETWEEN :startDate AND :endDate")
    Flux<Payment> findByGatewayAndStatusInDateRange(String gateway, String status,
                                                    LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT * FROM payments WHERE status = 'FAILED' AND retry_count < :maxRetries")
    Flux<Payment> findFailedPaymentsForRetry(int maxRetries);

    @Query("UPDATE payments SET retry_count = retry_count + 1 WHERE id = :id RETURNING *")
    Mono<Payment> incrementRetryCount(Long id);
}
