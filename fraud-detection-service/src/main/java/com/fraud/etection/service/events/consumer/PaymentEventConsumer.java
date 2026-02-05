package com.fraud.etection.service.events.consumer;


import com.fraud.etection.service.dto.FraudCheckRequest;
import com.fraud.etection.service.events.PaymentCreatedEvent;
import com.fraud.etection.service.service.FraudDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class PaymentEventConsumer {

    private final FraudDetectionService fraudDetectionService;

    public PaymentEventConsumer(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @KafkaListener(topics = "payment-created", groupId = "fraud-detection-group")
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        log.info("🎧 [FraudDetection] Payment created: {}", event.getPaymentId());

        FraudCheckRequest request = FraudCheckRequest.builder()
                .entityType("PAYMENT")
                .entityId(event.getPaymentId())
                .customerId(event.getCustomerId())
                .accountId(event.getAccountId())
                .amount(new BigDecimal(event.getAmount()))
                .currency(event.getCurrency())
                .ipAddress(event.getIpAddress())
                .deviceId(event.getDeviceId())
                .build();

        fraudDetectionService.performFraudCheck(request)
                .doOnSuccess(result -> {
                    log.info("🔍 Fraud check completed for payment: {} decision: {}",
                            event.getPaymentId(), result.getDecision());

                    if ("BLOCKED".equals(result.getDecision())) {
                        log.error("🚫 PAYMENT BLOCKED DUE TO FRAUD: {}", event.getPaymentId());
                        // In production, you'd call Payment Service to cancel this payment
                    }
                })
                .doOnError(error -> log.error("❌ Fraud check failed", error))
                .subscribe();
    }
}