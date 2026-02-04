package com.notification.service.event.consumer;


import com.notification.service.dto.SendNotificationRequest;
import com.notification.service.event.TransactionCompletedEvent;
import com.notification.service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionEventConsumer {

    private final NotificationService notificationService;

    public TransactionEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "transaction-completed", groupId = "notification-service-group")
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        log.info("🎧 [TransactionEvent] Received: transactionId={}, type={}", event.getTransactionId(), event.getTransactionType());

        String subject;
        String body;

        switch (event.getTransactionType()) {
            case "PAYMENT" -> {
                subject = "💳 Transaction Confirmed";
                body = "A payment of " + event.getAmount() + " " + event.getCurrency() +
                        " has been recorded.\n\nReference: " + event.getReferenceNumber() +
                        "\nTransaction ID: " + event.getTransactionId();
            }
            case "REFUND" -> {
                subject = "💰 Refund Processed";
                body = "A refund of " + event.getAmount() + " " + event.getCurrency() +
                        " has been processed to your account.\n\nReference: " + event.getReferenceNumber();
            }
            case "REVERSAL" -> {
                subject = "🔄 Transaction Reversed";
                body = "A transaction of " + event.getAmount() + " " + event.getCurrency() +
                        " has been reversed.\n\nReference: " + event.getReferenceNumber();
            }
            default -> {
                subject = "📋 Transaction Update";
                body = "Transaction " + event.getTransactionId() + " has been updated.";
            }
        }

        SendNotificationRequest request = SendNotificationRequest.builder()
                .customerId(event.getCustomerId())
                .customerEmail(event.getCustomerEmail())
                .notificationType("EMAIL")
                .notificationCategory("TRANSACTION")
                .subject(subject)
                .body(body)
                .templateId("TRANSACTION_" + event.getTransactionType())
                .sourceEventType("transaction-completed")
                .sourceEntityId(event.getTransactionId())
                .sourceEntityType("TRANSACTION")
                .build();

        notificationService.sendNotification(request)
                .doOnSuccess(n -> log.info("✅ Transaction notification sent: {}", n.getNotificationId()))
                .doOnError(e -> log.error("❌ Failed to send transaction notification", e))
                .subscribe();
    }
}