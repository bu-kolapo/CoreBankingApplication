package com.notification.service.event.consumer;


import com.notification.service.dto.SendNotificationRequest;
import com.notification.service.event.ReconciliationAlertEvent;
import com.notification.service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReconciliationEventConsumer {

    private final NotificationService notificationService;

    public ReconciliationEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Reconciliation alerts go to the OPS/ADMIN team, not to customers.
     */
    @KafkaListener(topics = "reconciliation-completed", groupId = "notification-service-group")
    public void handleReconciliationCompleted(ReconciliationAlertEvent event) {
        log.info("🎧 [ReconciliationEvent] Batch: {}, unmatched: {}", event.getBatchId(), event.getUnmatchedCount());

        // Only alert if there ARE discrepancies
        if (event.getUnmatchedCount() == 0 && event.getMissingInternalCount() == 0 && event.getMissingInGatewayCount() == 0) {
            log.info("✅ Reconciliation clean — no alert needed");
            return;
        }

        SendNotificationRequest request = SendNotificationRequest.builder()
                .customerId("SYSTEM")                          // Not a real customer
                .customerEmail(event.getAdminEmail())          // Goes to ops team
                .notificationType("EMAIL")
                .notificationCategory("RECONCILIATION")
                .subject("⚠️ Reconciliation Alert — Discrepancies Found")
                .body("Reconciliation batch " + event.getBatchId() + " for " + event.getProvider() + " completed with discrepancies:\n\n" +
                        "Unmatched: " + event.getUnmatchedCount() + "\n" +
                        "Missing in our system: " + event.getMissingInternalCount() + "\n" +
                        "Missing in gateway: " + event.getMissingInGatewayCount() + "\n\n" +
                        "Please review: GET /api/reconciliation/batches/" + event.getBatchId() + "/discrepancies")
                .templateId("RECONCILIATION_ALERT")
                .sourceEventType("reconciliation-completed")
                .sourceEntityId(event.getBatchId())
                .sourceEntityType("RECONCILIATION")
                .build();

        notificationService.sendNotification(request)
                .doOnSuccess(n -> log.info("✅ Reconciliation alert sent: {}", n.getNotificationId()))
                .doOnError(e -> log.error("❌ Failed to send reconciliation alert", e))
                .subscribe();
    }
}
