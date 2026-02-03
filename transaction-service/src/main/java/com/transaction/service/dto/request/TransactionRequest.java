package com.transaction.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    private String paymentId;
    private String orderId;
    private String accountId;
    private String transactionType;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String gatewayTransactionId;
    private String paymentGateway;
}
