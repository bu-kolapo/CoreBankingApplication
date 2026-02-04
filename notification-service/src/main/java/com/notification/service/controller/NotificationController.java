package com.notification.service.controller;


import com.notification.service.dto.NotificationDto;
import com.notification.service.dto.SendNotificationRequest;
import com.notification.service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Manual send (useful for testing or admin-triggered notifications)
     */
    @PostMapping("/send")
    public Mono<ResponseEntity<NotificationDto>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {

        log.info("📬 Manual notification request: type={}, customer={}", request.getNotificationType(), request.getCustomerId());

        return notificationService.sendNotification(request)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto))
                .onErrorResume(error -> {
                    log.error("❌ Failed to send notification", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Get all notifications for a customer
     */
    @GetMapping("/customer/{customerId}")
    public Flux<NotificationDto> getCustomerNotifications(@PathVariable String customerId) {
        log.info("🔍 Fetching notifications for customer: {}", customerId);
        return notificationService.getCustomerNotifications(customerId);
    }

    /**
     * Get notifications filtered by category
     */
    @GetMapping("/customer/{customerId}/category/{category}")
    public Flux<NotificationDto> getCustomerNotificationsByCategory(
            @PathVariable String customerId,
            @PathVariable String category) {

        log.info("🔍 Fetching {} notifications for customer: {}", category, customerId);
        return notificationService.getCustomerNotificationsByCategory(customerId, category);
    }

    /**
     * Manually trigger retry of all failed notifications
     */
    @PostMapping("/retry")
    public Mono<ResponseEntity<Void>> retryFailedNotifications() {
        log.info("🔄 Retrying failed notifications");

        return notificationService.retryFailedNotifications()
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(error -> {
                    log.error("❌ Retry failed", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}
