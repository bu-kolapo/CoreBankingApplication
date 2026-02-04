package com.notification.service.event.consumer;


import com.notification.service.dto.SendNotificationRequest;
import com.notification.service.event.PaymentCompletedEvent;
import com.notification.service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    public PaymentEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "payment-completed", groupId = "notification-service-group")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("🎧 [PaymentEvent] Received: paymentId={}, status={}", event.getPaymentId(), event.getStatus());

        if ("SUCCESS".equals(event.getStatus())) {
            sendPaymentSuccessNotification(event);
        } else if ("FAILED".equals(event.getStatus())) {
            sendPaymentFailedNotification(event);
        }
    }

    private void sendPaymentSuccessNotification(PaymentCompletedEvent event) {
        SendNotificationRequest emailRequest = SendNotificationRequest.builder()
                .customerId(event.getCustomerId())
                .customerEmail(event.getCustomerEmail())
                .notificationType("EMAIL")
                .notificationCategory("PAYMENT")
                .subject("✅ Payment Successful")
                .body("Your payment of " + event.getAmount() + " " + event.getCurrency() +
                        " for order " + event.getOrderId() + " was successful.\n\nPayment ID: " + event.getPaymentId())
                .templateId("PAYMENT_SUCCESS")
                .sourceEventType("payment-completed")
                .sourceEntityId(event.getPaymentId())
                .sourceEntityType("PAYMENT")
                .build();

        notificationService.sendNotification(emailRequest)
                .doOnSuccess(n -> log.info("✅ Payment success notification sent: {}", n.getNotificationId()))
                .doOnError(e -> log.error("❌ Failed to send payment success notification", e))
                .subscribe();
    }

    private void sendPaymentFailedNotification(PaymentCompletedEvent event) {
        SendNotificationRequest emailRequest = SendNotificationRequest.builder()
                .customerId(event.getCustomerId())
                .customerEmail(event.getCustomerEmail())
                .notificationType("EMAIL")
                .notificationCategory("PAYMENT")
                .subject("❌ Payment Failed")
                .body("Your payment of " + event.getAmount() + " " + event.getCurrency() +
                        " failed.\n\nReason: " + event.getFailureReason() + "\nPayment ID: " + event.getPaymentId() +
                        "\n\nPlease try again or contact support.")
                .templateId("PAYMENT_FAILED")
                .sourceEventType("payment-completed")
                .sourceEntityId(event.getPaymentId())
                .sourceEntityType("PAYMENT")
                .build();

        notificationService.sendNotification(emailRequest)
                .doOnSuccess(n -> log.info("✅ Payment failed notification sent: {}", n.getNotificationId()))
                .doOnError(e -> log.error("❌ Failed to send payment failed notification", e))
                .subscribe();
    }
}