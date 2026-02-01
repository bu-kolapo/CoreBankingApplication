package com.webhook.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("webhook_events")
public class WebhookEvent {
    @Id
    private Long id;

    private String webhookId;           // UUID
    private String eventId;             // External provider's event ID (for idempotency)
    private String eventType;           // payment.success, payment.failed, etc.
    private String provider;            // STRIPE, PAYPAL, etc.
    private String paymentId;           // Reference to our payment
    private String gatewayTransactionId; // Provider's transaction ID

    private String status;              // RECEIVED, PROCESSING, PROCESSED, FAILED
    private String payload;             // Full webhook payload (JSON)
    private String signature;           // Webhook signature for verification

    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
