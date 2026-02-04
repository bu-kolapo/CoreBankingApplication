package com.notification.service.event.consumer;

import com.notification.service.dto.SendNotificationRequest;
import com.notification.service.event.CustomerCreatedEvent;
import com.notification.service.event.KycStatusChangedEvent;
import com.notification.service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerEventConsumer {

    private final NotificationService notificationService;

    public CustomerEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "customer-created", groupId = "notification-service-group")
    public void handleCustomerCreated(CustomerCreatedEvent event) {
        log.info("🎧 [CustomerEvent] New customer: {}", event.getCustomerId());

        SendNotificationRequest request = SendNotificationRequest.builder()
                .customerId(event.getCustomerId())
                .customerEmail(event.getEmail())
                .notificationType("EMAIL")
                .notificationCategory("ACCOUNT")
                .subject("👋 Welcome to Our Banking Platform!")
                .body("Hello!\n\nWelcome to our platform. Your account has been created.\n\n" +
                        "Customer Number: " + event.getCustomerNumber() + "\n\n" +
                        "Next step: Please verify your email and complete KYC to unlock all features.")
                .templateId("WELCOME_EMAIL")
                .sourceEventType("customer-created")
                .sourceEntityId(event.getCustomerId())
                .sourceEntityType("CUSTOMER")
                .build();

        notificationService.sendNotification(request)
                .doOnSuccess(n -> log.info("✅ Welcome notification sent: {}", n.getNotificationId()))
                .doOnError(e -> log.error("❌ Failed to send welcome notification", e))
                .subscribe();
    }

    @KafkaListener(topics = "kyc-status-changed", groupId = "notification-service-group")
    public void handleKycStatusChanged(KycStatusChangedEvent event) {
        log.info("🎧 [KycEvent] Customer: {}, status: {}", event.getCustomerId(), event.getKycStatus());

        String subject;
        String body;

        if ("VERIFIED".equals(event.getKycStatus())) {
            subject = "✅ KYC Verified";
            body = "Your KYC verification is complete. You now have access to " + event.getKycLevel() + " features.";
        } else if ("REJECTED".equals(event.getKycStatus())) {
            subject = "❌ KYC Rejected";
            body = "Your KYC submission was not approved. Please resubmit your documents or contact support.";
        } else {
            subject = "📋 KYC Status Update";
            body = "Your KYC status has changed to: " + event.getKycStatus();
        }

        SendNotificationRequest request = SendNotificationRequest.builder()
                .customerId(event.getCustomerId())
                .customerEmail(event.getEmail())
                .notificationType("EMAIL")
                .notificationCategory("KYC")
                .subject(subject)
                .body(body)
                .templateId("KYC_" + event.getKycStatus())
                .sourceEventType("kyc-status-changed")
                .sourceEntityId(event.getCustomerId())
                .sourceEntityType("CUSTOMER")
                .build();

        notificationService.sendNotification(request)
                .doOnSuccess(n -> log.info("✅ KYC notification sent: {}", n.getNotificationId()))
                .doOnError(e -> log.error("❌ Failed to send KYC notification", e))
                .subscribe();
    }
}