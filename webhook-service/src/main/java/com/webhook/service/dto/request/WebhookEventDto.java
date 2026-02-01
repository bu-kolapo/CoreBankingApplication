package com.webhook.service.dto.request;

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
public class WebhookEventDto {

        private String eventId;
        private String eventType;
        private String provider;
        private String transactionId;
        private String paymentId;
        private String status;
        private BigDecimal amount;
        private String currency;
        private String signature;
        private Object data; // Full event data
}