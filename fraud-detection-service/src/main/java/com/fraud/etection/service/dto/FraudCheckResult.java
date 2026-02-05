package com.fraud.etection.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckResult {
    private String checkId;
    private String riskLevel;               // LOW, MEDIUM, HIGH, CRITICAL
    private BigDecimal riskScore;
    private String decision;                // APPROVED, FLAGGED, BLOCKED
    private List<String> flaggedReasons;
    private boolean requiresReview;
}