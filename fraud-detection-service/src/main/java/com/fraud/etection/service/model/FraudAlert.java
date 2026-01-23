package com.fraud.etection.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("fraud_alerts")
public class FraudAlert {
    @Id
    private Long id;
    private Long transactionId;
    private Long accountId;
    private String alertType; // SUSPICIOUS_AMOUNT, UNUSUAL_LOCATION, VELOCITY_CHECK
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String description;
    private String status; // OPEN, INVESTIGATING, RESOLVED, FALSE_POSITIVE
    private String resolvedBy;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}