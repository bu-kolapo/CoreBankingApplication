package com.commonlib.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {
    private String eventId;
    private String paymentId;
    private String eventType; // INITIATED, SUCCESS, FAILED
    private BigDecimal amount;
    private String currency;
    private String orderId;
    private LocalDateTime timestamp;
    private String status;
    private String gatewayTransactionId;
}