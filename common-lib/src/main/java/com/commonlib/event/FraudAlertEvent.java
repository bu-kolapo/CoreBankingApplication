package com.commonlib.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlertEvent {
    private String alertId;
    private String transactionId;
    private Long accountId;
    private String severity;
    private String alertType;
    private Double riskScore;
    private LocalDateTime timestamp;
}