package com.webhook.service.repository;

import com.webhook.service.model.WebhookEvent;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface WebhookEventRepository extends ReactiveCrudRepository<WebhookEvent, Long> {

        Mono<WebhookEvent> findByEventId(String eventId);

        Mono<WebhookEvent> findByWebhookId(String webhookId);

        Flux<WebhookEvent> findByPaymentId(String paymentId);

        Flux<WebhookEvent> findByStatusAndCreatedAtBefore(String status, LocalDateTime before);

        @Query("SELECT * FROM webhooks WHERE status = :status AND retry_count < :maxRetries ORDER BY created_at ASC LIMIT :limit")
        Flux<WebhookEvent> findFailedWebhooksForRetry(String status, int maxRetries, int limit);
    }