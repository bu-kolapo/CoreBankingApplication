package com.fraud.etection.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Table("fraud_checks")
    public class FraudCheck {

        @Id
        private Long id;

        private String checkId;                 // UUID
        private String entityType;              // PAYMENT, TRANSACTION, CUSTOMER
        private String entityId;                // The ID of what we're checking

        private String customerId;
        private String accountId;
        private BigDecimal amount;
        private String currency;

        // Check result
        private String riskLevel;               // LOW, MEDIUM, HIGH, CRITICAL
        private BigDecimal riskScore;           // 0.0 - 100.0
        private String decision;                // APPROVED, FLAGGED, BLOCKED
        private String flaggedReasons;          // JSON array of triggered rules

        // Rules triggered
        private Boolean velocityCheckFailed;
        private Boolean amountThresholdExceeded;
        private Boolean suspiciousLocationDetected;
        private Boolean blacklistedUser;
        private Boolean unusualPatternDetected;

        // Context
        private String ipAddress;
        private String deviceId;
        private String location;                // JSON: {country, city, lat, lng}
        private String userAgent;

        // Resolution
        private String reviewStatus;            // PENDING, REVIEWED, RESOLVED
        private String reviewedBy;
        private String reviewNotes;
        private LocalDateTime reviewedAt;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
