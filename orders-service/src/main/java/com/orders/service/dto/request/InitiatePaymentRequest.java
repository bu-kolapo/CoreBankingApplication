package com.orders.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentRequest {
    private String orderId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String customerEmail;
    private String idempotencyKey;
}