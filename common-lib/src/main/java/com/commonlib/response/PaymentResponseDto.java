package com.commonlib.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {
    private String paymentId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String gatewayTransactionId;
    private String checkoutUrl;
    private LocalDateTime createdAt;
}