package com.payments.service.dto.request;



import jakarta.validation.constraints.*;
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
public class PaymentDto {
    private Long id;
    private String  idempotencyKey;
    private String paymentId;
    private Long orderId;
    private Long accountId;
    private String paymentGateway;
    private String callbackUrl;
    private String webhookUrl;
    private String gatewayTransactionId;
    private BigDecimal amount;
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
