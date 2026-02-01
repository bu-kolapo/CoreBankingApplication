package com.webhook.service.service;

import com.webhook.service.dto.request.WebhookEventDto;
import com.webhook.service.dto.response.WebhookResponseDto;
import reactor.core.publisher.Mono;

public interface WebhookService {
    Mono<WebhookResponseDto> handleWebhook(WebhookEventDto event, String rawPayload);

    Mono<Boolean> verifyWebhookSignature(String payload, String signature, String provider);

    Mono<Void> retryFailedWebhooks();
}
