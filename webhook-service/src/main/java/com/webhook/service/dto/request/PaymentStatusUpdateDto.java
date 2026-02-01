package com.webhook.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusUpdateDto {
    private String paymentId;
    private String gatewayTransactionId;
    private String status; // SUCCESS, FAILED, REFUNDED
    private String failureReason;
    private String providerResponse;
}