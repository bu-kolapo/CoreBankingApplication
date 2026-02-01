package com.webhook.service.controller;



import com.webhook.service.dto.request.WebhookEventDto;
import com.webhook.service.dto.response.WebhookResponseDto;
import com.webhook.service.service.WebhookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/stripe")
    public Mono<ResponseEntity<WebhookResponseDto>> handleStripeWebhook(
            @RequestBody String rawPayload,
            @RequestHeader("Stripe-Signature") String signature) {

        log.info("🎣 Received Stripe webhook");

        return Mono.fromCallable(() -> parseStripeEvent(rawPayload))
                .flatMap(event -> {
                    event.setProvider("STRIPE");
                    event.setSignature(signature);

                    // Verify signature first
                    return webhookService.verifyWebhookSignature(rawPayload, signature, "STRIPE")
                            .flatMap(isValid -> {
                                if (!isValid) {
                                    log.error("❌ Invalid webhook signature");
                                    return Mono.just(ResponseEntity
                                            .status(HttpStatus.UNAUTHORIZED)
                                            .body(WebhookResponseDto.builder()
                                                    .status("INVALID_SIGNATURE")
                                                    .message("Invalid webhook signature")
                                                    .processed(false)
                                                    .build()));
                                }

                                return webhookService.handleWebhook(event, rawPayload)
                                        .map(response -> ResponseEntity.ok(response));
                            });
                })
                .onErrorResume(error -> {
                    log.error("❌ Webhook processing error", error);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(WebhookResponseDto.builder()
                                    .status("ERROR")
                                    .message(error.getMessage())
                                    .processed(false)
                                    .build()));
                });
    }

    @PostMapping("/paypal")
    public Mono<ResponseEntity<WebhookResponseDto>> handlePayPalWebhook(
            @RequestBody WebhookEventDto event,
            @RequestHeader("Paypal-Transmission-Sig") String signature) {

        log.info("🎣 Received PayPal webhook");
        event.setProvider("PAYPAL");
        event.setSignature(signature);

        // Similar implementation as Stripe
        return webhookService.handleWebhook(event, event.toString())
                .map(ResponseEntity::ok)
                .onErrorResume(error ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(WebhookResponseDto.builder()
                                        .status("ERROR")
                                        .message(error.getMessage())
                                        .processed(false)
                                        .build())));
    }

    private WebhookEventDto parseStripeEvent(String rawPayload) {
        // Parse Stripe-specific webhook format
        // This is simplified - actual implementation would parse Stripe's JSON structure
        return WebhookEventDto.builder()
                .eventId("evt_" + System.currentTimeMillis())
                .eventType("payment.success")
                .build();
    }
}