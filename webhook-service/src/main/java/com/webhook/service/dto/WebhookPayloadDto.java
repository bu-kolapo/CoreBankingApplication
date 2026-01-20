package com.webhook.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookPayloadDto {
    private String eventId;
    private String eventType;
    private String paymentId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String gatewayTransactionId;
    private LocalDateTime eventTimestamp;
    private Object metadata;
}