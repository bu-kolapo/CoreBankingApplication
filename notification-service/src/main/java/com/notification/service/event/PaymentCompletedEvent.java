package com.notification.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCompletedEvent {

    private String paymentId;
    private String orderId;
    private String accountId;
    private String customerId;
    private String amount;
    private String currency;
    private String status;                  // SUCCESS or FAILED
    private String customerEmail;
    private String failureReason;           // populated if FAILED
}
