package com.fraud.etection.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class FraudAlertDto {
    private String alertId;
    private Long transactionId;
    private String severity;
    private String alertType;
    private Double riskScore;
    private String status;
}
