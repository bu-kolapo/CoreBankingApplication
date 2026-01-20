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
    private String eventId; // Unique event identifier
    private String eventType; // PAYMENT_SUCCESS, PAYMENT_FAILED, REFUND_INITIATED
    private String source; // STRIPE, PAYPAL, etc.
    private String paymentId;
    private String payload; // JSON payload
    private String signature; // For verification
    private String status; // RECEIVED, PROCESSING, PROCESSED, FAILED
    private Integer processingAttempts;
    private String processingError;
    private LocalDateTime receivedAt;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
