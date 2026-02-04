package com.notification.service.event.consumer;


import com.notification.service.dto.SendNotificationRequest;
import com.notification.service.event.AccountEventDto;
import com.notification.service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountEventConsumer {

    private final NotificationService notificationService;

    public AccountEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "account-frozen", groupId = "notification-service-group")
    public void handleAccountFrozen(AccountEventDto event) {
        log.info("🎧 [AccountEvent] Account frozen: {}", event.getAccountId());

        SendNotificationRequest request = SendNotificationRequest.builder()
                .customerId(event.getCustomerId())
                .customerEmail(event.getEmail())
                .notificationType("EMAIL")
                .notificationCategory("ACCOUNT")
                .subject("❄️ Account Frozen")
                .body("Your account " + event.getAccountNumber() + " has been frozen.\n\n" +
                        "If you did not expect this, please contact support immediately.")
                .templateId("ACCOUNT_FROZEN")
                .sourceEventType("account-frozen")
                .sourceEntityId(event.getAccountId())
                .sourceEntityType("ACCOUNT")
                .build();

        notificationService.sendNotification(request)
                .doOnSuccess(n -> log.info("✅ Account frozen notification sent: {}", n.getNotificationId()))
                .doOnError(e -> log.error("❌ Failed to send account frozen notification", e))
                .subscribe();
    }

    @KafkaListener(topics = "account-closed", groupId = "notification-service-group")
    public void handleAccountClosed(AccountEventDto event) {
        log.info("🎧 [AccountEvent] Account closed: {}", event.getAccountId());

        SendNotificationRequest request = SendNotificationRequest.builder()
                .customerId(event.getCustomerId())
                .customerEmail(event.getEmail())
                .notificationType("EMAIL")
                .notificationCategory("ACCOUNT")
                .subject("🔒 Account Closed")
                .body("Your account " + event.getAccountNumber() + " has been successfully closed.\n\n" +
                        "If you change your mind, please contact support.")
                .templateId("ACCOUNT_CLOSED")
                .sourceEventType("account-closed")
                .sourceEntityId(event.getAccountId())
                .sourceEntityType("ACCOUNT")
                .build();

        notificationService.sendNotification(request)
                .doOnSuccess(n -> log.info("✅ Account closed notification sent: {}", n.getNotificationId()))
                .doOnError(e -> log.error("❌ Failed to send account closed notification", e))
                .subscribe();
    }
}