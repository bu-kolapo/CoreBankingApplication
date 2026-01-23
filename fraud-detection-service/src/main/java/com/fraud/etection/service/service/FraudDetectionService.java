package com.fraud.etection.service.service;

import com.fraud.etection.service.dto.FraudAlertDto;
import com.fraud.etection.service.dto.FraudDetectionRequestDto;
import com.fraud.etection.service.dto.FraudDetectionResponseDto;
import reactor.core.publisher.Mono;

public interface FraudDetectionService {
    /**
     * Analyze transaction for fraud
     */
    Mono<FraudDetectionResponseDto> analyzeTransaction(FraudDetectionRequestDto request);

    /**
     * Get fraud alert by ID
     */
    Mono<FraudAlertDto> getFraudAlert(String alertId);

    /**
     * Update fraud alert status
     */
    Mono<Void> updateAlertStatus(String alertId, String status, String resolution);

}
