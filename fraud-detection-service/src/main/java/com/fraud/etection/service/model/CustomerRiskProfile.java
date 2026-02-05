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
@Table("customer_risk_profiles")
public class CustomerRiskProfile {

    @Id
    private Long id;

    private String profileId;               // UUID
    private String customerId;

    // Overall risk
    private String riskRating;              // LOW, MEDIUM, HIGH, CRITICAL
    private BigDecimal riskScore;           // 0.0 - 100.0

    // Behavior tracking
    private Integer totalTransactions;
    private BigDecimal totalTransactionVolume;
    private BigDecimal avgTransactionAmount;
    private BigDecimal maxTransactionAmount;

    // Flags
    private Integer fraudIncidents;
    private Integer chargebacks;
    private Integer failedTransactions;

    // Activity patterns
    private String commonLocations;         // JSON array of frequent locations
    private String commonDevices;           // JSON array of device fingerprints
    private String transactionPatterns;     // JSON: time-of-day patterns, frequency, etc.

    // Blacklist / Whitelist
    private Boolean blacklisted;
    private String blacklistReason;
    private LocalDateTime blacklistedAt;

    private Boolean whitelisted;
    private LocalDateTime whitelistedAt;

    // Last activity
    private LocalDateTime lastTransactionAt;
    private LocalDateTime lastFraudCheckAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
