package com.payments.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudDetectionResponseDto {
    private String alertId;
    private String riskLevel;
    private Double riskScore;
    private String[] detectedPatterns;
    private boolean approved;
    private String recommendedAction;
}
