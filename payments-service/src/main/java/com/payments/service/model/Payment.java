package com.payments.service.model;

// payments-domain/src/main/java/com.bank.payments/model/Payment.java
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("payments")
public class Payment {
    private Long id;
    private String  idempotencyKey;
    private String paymentId;
    private Long orderId;
    private Long accountId;
    private String paymentGateway;
    private String gatewayTransactionId;
    private BigDecimal amount;
    private String callbackUrl;
    private String webhookUrl;
    private String currency;
    private String status;
    private String paymentMethod;
    private String customerEmail;
    private Integer retryCount;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
