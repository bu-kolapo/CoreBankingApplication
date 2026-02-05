package com.fraud.etection.service.events.consumer;


import com.fraud.etection.service.dto.FraudCheckRequest;
import com.fraud.etection.service.events.TransactionCompletedEvent;
import com.fraud.etection.service.service.FraudDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class TransactionEventConsumer {

    private final FraudDetectionService fraudDetectionService;

    public TransactionEventConsumer(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @KafkaListener(topics = "transaction-completed", groupId = "fraud-detection-group")
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        log.info("🎧 [FraudDetection] Transaction completed: {}", event.getTransactionId());

        FraudCheckRequest request = FraudCheckRequest.builder()
                .entityType("TRANSACTION")
                .entityId(event.getTransactionId())
                .customerId(event.getCustomerId())
                .accountId(event.getAccountId())
                .amount(new BigDecimal(event.getAmount()))
                .currency(event.getCurrency())
                .build();

        fraudDetectionService.performFraudCheck(request)
                .flatMap(result -> {
                    log.info("🔍 Fraud check for transaction: {} decision: {}",
                            event.getTransactionId(), result.getDecision());

                    // Update customer risk profile after each transaction
                    return fraudDetectionService.updateCustomerRiskProfile(event.getCustomerId())
                            .thenReturn(result);
                })
                .doOnError(error -> log.error("❌ Fraud check failed", error))
                .subscribe();
    }
}