package com.orders.service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    private String paymentId;
    private String orderId;
    private String accountId;
    private String customerId;
    private String amount;
    private String currency;
    private String status;                  // SUCCESS, FAILED
    private String gatewayTransactionId;
}