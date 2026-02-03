package com.transaction.service.events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestEvent {
    private String paymentId;
    private String transactionId;
    private String orderId;
    private String accountId;
    private String amount;
    private String currency;
    private String reason;
}
