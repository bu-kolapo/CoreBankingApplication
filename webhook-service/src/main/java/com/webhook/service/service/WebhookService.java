package com.webhook.service.service;

import com.webhook.service.dto.WebhookEventDto;
import reactor.core.publisher.Mono;

public interface WebhookService {
    /**
     * Handle incoming webhook from payment gateway
     */
    Mono<Void> handleWebhook(String source, String signature, WebhookEventDto payload);

    /**
     * Verify webhook signature
     */
    Mono<Boolean> verifyWebhookSignature(String source, String signature, String payload);

    /**
     * Get webhook event by ID
     */
    Mono<WebhookEventDto> getWebhookEvent(String eventId);

    /**
     * Retry failed webhook processing
     */
    Mono<Void> retryWebhookEvent(String eventId);
}
