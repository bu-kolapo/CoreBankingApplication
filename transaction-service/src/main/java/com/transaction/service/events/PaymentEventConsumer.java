package com.transaction.service.events;


import com.transaction.service.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PaymentEventConsumer {

    private final TransactionService transactionService;

    public PaymentEventConsumer(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(
            topics = "payment-completed",
            groupId = "transaction-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("🎧 Received payment completed event: {}", event.getPaymentId());

        transactionService.handlePaymentCompleted(event)
                .doOnSuccess(transaction ->
                        log.info("✅ Transaction created from payment: {}", transaction.getTransactionId()))
                .doOnError(error ->
                        log.error("❌ Failed to create transaction from payment", error))
                .subscribe();
    }

    @KafkaListener(
            topics = "refund-requested",
            groupId = "transaction-service-group"
    )
    public void handleRefundRequested(RefundRequestEvent event) {
        log.info("🎧 Received refund request: {}", event.getPaymentId());

        transactionService.processRefund(event)
                .doOnSuccess(transaction ->
                        log.info("✅ Refund processed: {}", transaction.getTransactionId()))
                .doOnError(error ->
                        log.error("❌ Failed to process refund", error))
                .subscribe();
    }
}