package com.fraud.etection.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckRequest {
    private String entityType;              // PAYMENT, TRANSACTION
    private String entityId;

    private String customerId;
    private String accountId;
    private BigDecimal amount;
    private String currency;

    private String ipAddress;
    private String deviceId;
    private String location;                // JSON: {country, city}
    private String userAgent;
}