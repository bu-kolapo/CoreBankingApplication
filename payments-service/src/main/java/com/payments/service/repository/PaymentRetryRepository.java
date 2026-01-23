package com.payments.service.repository;

import com.payments.service.model.PaymentRetry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Repository
public interface PaymentRetryRepository extends R2dbcRepository<PaymentRetry, Long> {
    Flux<PaymentRetry> findByPaymentId(Long paymentId);

    @Query("SELECT * FROM payment_retries WHERE status = 'SCHEDULED' " +
            "AND scheduled_at <= :currentTime ORDER BY scheduled_at ASC")
    Flux<PaymentRetry> findScheduledRetries(LocalDateTime currentTime);
}