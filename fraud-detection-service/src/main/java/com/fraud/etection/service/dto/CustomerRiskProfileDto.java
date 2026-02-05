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
public class CustomerRiskProfileDto {
    private String profileId;
    private String customerId;
    private String riskRating;
    private BigDecimal riskScore;
    private Integer totalTransactions;
    private Integer fraudIncidents;
    private Boolean blacklisted;
    private Boolean whitelisted;
}