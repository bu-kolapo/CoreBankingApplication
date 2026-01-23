package com.webhook.service.repository;

import com.webhook.service.model.WebhookEvent;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface WebhookEventRepository extends R2dbcRepository<WebhookEvent, Long> {
    Mono<WebhookEvent> findByEventId(String eventId);
    Flux<WebhookEvent> findByPaymentId(String paymentId);
    Flux<WebhookEvent> findByStatus(String status);

    @Query("SELECT * FROM webhook_events WHERE status = 'FAILED' " +
            "AND processing_attempts < :maxAttempts ORDER BY received_at ASC")
    Flux<WebhookEvent> findFailedEventsForRetry(int maxAttempts);

    @Query("UPDATE webhook_events SET processing_attempts = processing_attempts + 1, " +
            "status = :status, processing_error = :error WHERE id = :id RETURNING *")
    Mono<WebhookEvent> updateProcessingStatus(Long id, String status, String error);
}