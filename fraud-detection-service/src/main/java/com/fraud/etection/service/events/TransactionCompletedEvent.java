package com.fraud.etection.service.events;

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
    private String customerId;
    private String accountId;
    private String amount;
    private String currency;
    private String transactionType;
}