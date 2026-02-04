package com.notification.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCompletedEvent {
    private String transactionId;
    private String paymentId;
    private String accountId;
    private String customerId;
    private String amount;
    private String currency;
    private String transactionType;         // PAYMENT, REFUND, REVERSAL
    private String referenceNumber;
    private String customerEmail;
}