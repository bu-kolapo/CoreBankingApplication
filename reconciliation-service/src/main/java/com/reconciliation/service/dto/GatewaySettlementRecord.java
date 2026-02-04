package com.reconciliation.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewaySettlementRecord {
    private String transactionId;           // Stripe's transaction ID
    private BigDecimal amount;
    private String currency;
    private String status;                  // succeeded, failed, refunded
    private String settledAt;
    private String paymentIntentId;         // pi_xxx
}