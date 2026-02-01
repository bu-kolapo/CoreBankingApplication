package com.webhook.service.service.Implementation;

import com.webhook.service.dto.request.PaymentStatusUpdateDto;
import com.webhook.service.dto.request.WebhookEventDto;
import com.webhook.service.dto.response.WebhookResponseDto;
import com.webhook.service.model.WebhookEvent;
import com.webhook.service.repository.WebhookEventRepository;
import com.webhook.service.service.WebhookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;


@Slf4j
@Service
public class WebhookServiceImpl  implements WebhookService {




        private final WebhookEventRepository webhookRepository;
        private final ObjectMapper objectMapper;
        private final KafkaTemplate<String, Object> kafkaTemplate;

        @Value("${webhook.stripe.secret}")
        private String stripeSecret;

        @Value("${webhook.max-retries:3}")
        private int maxRetries;

        public WebhookServiceImpl(
                WebhookEventRepository webhookRepository,
                ObjectMapper objectMapper,
                KafkaTemplate<String, Object> kafkaTemplate) {
            this.webhookRepository = webhookRepository;
            this.objectMapper = objectMapper;
            this.kafkaTemplate = kafkaTemplate;
        }

        @Override
        @Transactional
        public Mono<WebhookResponseDto> handleWebhook(WebhookEventDto event, String rawPayload) {
            log.info("🎣 Received webhook event: {} from {}", event.getEventId(), event.getProvider());

            // Step 1: Check idempotency using eventId
            return webhookRepository.findByEventId(event.getEventId())
                    .flatMap(existing -> {
                        log.info("♻️ Duplicate webhook detected: {}", event.getEventId());
                        return Mono.just(WebhookResponseDto.builder()
                                .webhookId(existing.getWebhookId())
                                .eventId(existing.getEventId())
                                .status(existing.getStatus())
                                .message("Webhook already processed")
                                .processed(true)
                                .build());
                    })
                    .switchIfEmpty(Mono.defer(() -> processNewWebhook(event, rawPayload)));
        }

        private Mono<WebhookResponseDto> processNewWebhook(WebhookEventDto event, String rawPayload) {
            log.info("🆕 Processing new webhook: {}", event.getEventId());

            WebhookEvent webhook = WebhookEvent.builder()
                    .webhookId(UUID.randomUUID().toString())
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .provider(event.getProvider())
                    .paymentId(event.getPaymentId())
                    .gatewayTransactionId(event.getTransactionId())
                    .status("RECEIVED")
                    .payload(rawPayload)
                    .signature(event.getSignature())
                    .retryCount(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            return webhookRepository.save(webhook)
                    .flatMap(saved -> processWebhookEvent(saved, event))
                    .map(processed -> WebhookResponseDto.builder()
                            .webhookId(processed.getWebhookId())
                            .eventId(processed.getEventId())
                            .status(processed.getStatus())
                            .message("Webhook processed successfully")
                            .processed(true)
                            .build())
                    .onErrorResume(error -> handleWebhookError(webhook, error));
        }

        private Mono<WebhookEvent> processWebhookEvent(WebhookEvent webhook, WebhookEventDto event) {
            log.info("⚙️ Processing webhook event type: {}", event.getEventType());

            webhook.setStatus("PROCESSING");
            webhook.setUpdatedAt(LocalDateTime.now());

            return webhookRepository.save(webhook)
                    .flatMap(updated -> {
                        // Route based on event type
                        return switch (event.getEventType().toLowerCase()) {
                            case "payment.success", "payment.succeeded", "charge.succeeded" ->
                                    handlePaymentSuccess(updated, event);
                            case "payment.failed", "payment.failure", "charge.failed" ->
                                    handlePaymentFailure(updated, event);
                            case "payment.refunded", "charge.refunded" ->
                                    handlePaymentRefund(updated, event);
                            case "payment.disputed", "charge.dispute.created" ->
                                    handlePaymentDispute(updated, event);
                            default -> {
                                log.warn("⚠️ Unknown event type: {}", event.getEventType());
                                yield Mono.just(updated);
                            }
                        };
                    });
        }

        private Mono<WebhookEvent> handlePaymentSuccess(WebhookEvent webhook, WebhookEventDto event) {
            log.info("✅ Processing payment success for: {}", event.getPaymentId());

            PaymentStatusUpdateDto updateDto = PaymentStatusUpdateDto.builder()
                    .paymentId(event.getPaymentId())
                    .gatewayTransactionId(event.getTransactionId())
                    .status("SUCCESS")
                    .providerResponse(webhook.getPayload())
                    .build();

            return publishPaymentStatusUpdate(updateDto)
                    .then(markWebhookProcessed(webhook, "Payment success processed"));
        }

        private Mono<WebhookEvent> handlePaymentFailure(WebhookEvent webhook, WebhookEventDto event) {
            log.error("❌ Processing payment failure for: {}", event.getPaymentId());

            // Extract failure reason from payload
            String failureReason = extractFailureReason(event.getData());

            PaymentStatusUpdateDto updateDto = PaymentStatusUpdateDto.builder()
                    .paymentId(event.getPaymentId())
                    .gatewayTransactionId(event.getTransactionId())
                    .status("FAILED")
                    .failureReason(failureReason)
                    .providerResponse(webhook.getPayload())
                    .build();

            return publishPaymentStatusUpdate(updateDto)
                    .then(markWebhookProcessed(webhook, "Payment failure processed"));
        }

        private Mono<WebhookEvent> handlePaymentRefund(WebhookEvent webhook, WebhookEventDto event) {
            log.info("💰 Processing payment refund for: {}", event.getPaymentId());

            PaymentStatusUpdateDto updateDto = PaymentStatusUpdateDto.builder()
                    .paymentId(event.getPaymentId())
                    .gatewayTransactionId(event.getTransactionId())
                    .status("REFUNDED")
                    .providerResponse(webhook.getPayload())
                    .build();

            return publishPaymentStatusUpdate(updateDto)
                    .then(markWebhookProcessed(webhook, "Payment refund processed"));
        }

        private Mono<WebhookEvent> handlePaymentDispute(WebhookEvent webhook, WebhookEventDto event) {
            log.warn("⚠️ Processing payment dispute for: {}", event.getPaymentId());

            PaymentStatusUpdateDto updateDto = PaymentStatusUpdateDto.builder()
                    .paymentId(event.getPaymentId())
                    .gatewayTransactionId(event.getTransactionId())
                    .status("DISPUTED")
                    .providerResponse(webhook.getPayload())
                    .build();

            return publishPaymentStatusUpdate(updateDto)
                    .then(markWebhookProcessed(webhook, "Payment dispute processed"));
        }

        private Mono<Void> publishPaymentStatusUpdate(PaymentStatusUpdateDto updateDto) {
            return Mono.fromRunnable(() -> {
                try {
                    kafkaTemplate.send("payment-status-updates", updateDto.getPaymentId(), updateDto);
                    log.info("📤 Published payment status update: {}", updateDto.getPaymentId());
                } catch (Exception e) {
                    log.error("❌ Failed to publish payment status update", e);
                    throw new RuntimeException("Failed to publish payment status update", e);
                }
            });
        }

        private Mono<WebhookEvent> markWebhookProcessed(WebhookEvent webhook, String message) {
            webhook.setStatus("PROCESSED");
            webhook.setProcessedAt(LocalDateTime.now());
            webhook.setUpdatedAt(LocalDateTime.now());

            return webhookRepository.save(webhook)
                    .doOnSuccess(w -> log.info("✅ {}: {}", message, w.getWebhookId()));
        }

        private Mono<WebhookResponseDto> handleWebhookError(WebhookEvent webhook, Throwable error) {
            log.error("❌ Webhook processing failed: {}", webhook.getEventId(), error);

            webhook.setStatus("FAILED");
            webhook.setErrorMessage(error.getMessage());
            webhook.setRetryCount(webhook.getRetryCount() + 1);
            webhook.setUpdatedAt(LocalDateTime.now());

            return webhookRepository.save(webhook)
                    .map(failed -> WebhookResponseDto.builder()
                            .webhookId(failed.getWebhookId())
                            .eventId(failed.getEventId())
                            .status("FAILED")
                            .message("Webhook processing failed: " + error.getMessage())
                            .processed(false)
                            .build());
        }

        @Override
        public Mono<Boolean> verifyWebhookSignature(String payload, String signature, String provider) {
            return Mono.fromCallable(() -> {
                try {
                    String secret = getSecretForProvider(provider);
                    String computedSignature = computeHmacSha256(payload, secret);

                    boolean isValid = computedSignature.equals(signature);
                    log.info("🔐 Webhook signature verification: {}", isValid ? "VALID" : "INVALID");

                    return isValid;
                } catch (Exception e) {
                    log.error("❌ Signature verification failed", e);
                    return false;
                }
            });
        }

        @Override
        public Mono<Void> retryFailedWebhooks() {
            log.info("🔄 Retrying failed webhooks...");

            return webhookRepository.findFailedWebhooksForRetry("FAILED", maxRetries, 10)
                    .flatMap(webhook -> {
                        try {
                            WebhookEventDto event = objectMapper.readValue(
                                    webhook.getPayload(), WebhookEventDto.class);

                            return processWebhookEvent(webhook, event)
                                    .doOnSuccess(w -> log.info("✅ Retry successful: {}", w.getWebhookId()))
                                    .onErrorResume(error -> {
                                        log.error("❌ Retry failed: {}", webhook.getWebhookId(), error);
                                        return Mono.just(webhook);
                                    });
                        } catch (Exception e) {
                            log.error("❌ Failed to parse webhook payload", e);
                            return Mono.just(webhook);
                        }
                    })
                    .then();
        }

        // Helper methods
        private String getSecretForProvider(String provider) {
            return switch (provider.toUpperCase()) {
                case "STRIPE" -> stripeSecret;
                // Add other providers
                default -> throw new IllegalArgumentException("Unknown provider: " + provider);
            };
        }

        private String computeHmacSha256(String data, String secret) throws Exception {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        }

        private String extractFailureReason(Object data) {
            try {
                // Parse the data object to extract failure reason
                // This will vary based on provider
                return objectMapper.writeValueAsString(data);
            } catch (Exception e) {
                return "Unknown failure reason";
            }
        }
    }

