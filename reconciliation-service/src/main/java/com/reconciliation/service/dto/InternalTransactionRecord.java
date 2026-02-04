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
public class InternalTransactionRecord {

    private String transactionId;           // Our internal txn ID
    private String paymentId;               // Our payment ID
    private String gatewayTransactionId;    // Stripe's ID we stored
    private BigDecimal amount;
    private String currency;
    private String status;
}
