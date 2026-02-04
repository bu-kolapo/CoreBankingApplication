package com.notification.service.scheduler;



import com.notification.service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationRetryScheduler {

    private final NotificationService notificationService;

    public NotificationRetryScheduler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Every 5 minutes, retry any notifications that failed
     */
    @Scheduled(fixedDelayString = "${notification.retry-interval-ms:300000}")
    public void retryFailedNotifications() {
        log.info("⏰ Scheduled retry for failed notifications");

        notificationService.retryFailedNotifications()
                .doOnSuccess(v -> log.info("✅ Retry cycle complete"))
                .doOnError(e -> log.error("❌ Retry cycle failed", e))
                .subscribe();
    }
}
