package com.commonlib.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent implements Serializable {
    private String eventId;
    private String failureReason;
    private String paymentId;
    private String eventType; // INITIATED, SUCCESS, FAILED
    private BigDecimal amount;
    private String currency;
    private Long orderId;
    private LocalDateTime timestamp;
    private String status;
    private String gatewayTransactionId;
}