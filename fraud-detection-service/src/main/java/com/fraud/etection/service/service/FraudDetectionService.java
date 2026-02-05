package com.fraud.etection.service.service;

import com.fraud.etection.service.dto.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FraudDetectionService {
    // Core fraud check
    Mono<FraudCheckResult> performFraudCheck(FraudCheckRequest request);

    // Get fraud check details
    Mono<FraudCheckResult> getFraudCheck(String checkId);

    // Get all fraud checks for an entity
    Flux<FraudCheckResult> getFraudChecksForEntity(String entityType, String entityId);

    // Get flagged/blocked items pending review
    Flux<FraudCheckResult> getFlaggedChecks();

    Flux<FraudCheckResult> getBlockedChecks();

    // Risk profile management
    Mono<CustomerRiskProfileDto> getCustomerRiskProfile(String customerId);

    Mono<CustomerRiskProfileDto> updateCustomerRiskProfile(String customerId);

    // Blacklist / Whitelist
    Mono<Void> blacklistCustomer(String customerId, String reason);

    Mono<Void> whitelistCustomer(String customerId);

    Mono<Void> removeFromBlacklist(String customerId);

    // Manual review
    Mono<Void> approveCheck(String checkId, String reviewedBy, String notes);

    Mono<Void> rejectCheck(String checkId, String reviewedBy, String notes);
}


