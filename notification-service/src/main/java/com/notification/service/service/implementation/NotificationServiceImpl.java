package com.notification.service.service.implementation;


import com.notification.service.client.EmailClient;
import com.notification.service.client.SmsClient;
import com.notification.service.dto.NotificationDto;
import com.notification.service.dto.SendNotificationRequest;
import com.notification.service.model.Notification;
import com.notification.service.repository.NotificationRepository;
import com.notification.service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailClient emailClient;
    private final SmsClient smsClient;

    @Value("${notification.max-retries:3}")
    private int maxRetries;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            EmailClient emailClient,
            SmsClient smsClient) {
        this.notificationRepository = notificationRepository;
        this.emailClient = emailClient;
        this.smsClient = smsClient;
    }

    @Override
    @Transactional
    public Mono<NotificationDto> sendNotification(SendNotificationRequest request) {
        log.info("📬 Creating notification: type={}, category={}, customer={}",
                request.getNotificationType(), request.getNotificationCategory(), request.getCustomerId());

        // Build and persist the notification record first
        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .customerId(request.getCustomerId())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .notificationType(request.getNotificationType())
                .notificationCategory(request.getNotificationCategory())
                .status("PENDING")
                .subject(request.getSubject())
                .body(request.getBody())
                .templateId(request.getTemplateId())
                .sourceEventType(request.getSourceEventType())
                .sourceEntityId(request.getSourceEntityId())
                .sourceEntityType(request.getSourceEntityType())
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification)
                .flatMap(saved -> dispatchNotification(saved));
    }

    // Routes to the right client based on type
    private Mono<NotificationDto> dispatchNotification(Notification notification) {
        return switch (notification.getNotificationType()) {
            case "EMAIL" -> sendEmail(notification);
            case "SMS"   -> sendSms(notification);
            default -> {
                log.warn("⚠️ Unknown notification type: {}", notification.getNotificationType());
                yield Mono.just(toDto(notification));
            }
        };
    }

    private Mono<NotificationDto> sendEmail(Notification notification) {
        return emailClient.send(
                        notification.getCustomerEmail(),
                        notification.getSubject(),
                        notification.getBody())
                .flatMap(providerId -> {
                    notification.setStatus("SENT");
                    notification.setProviderMessageId(providerId);
                    notification.setSentAt(LocalDateTime.now());
                    notification.setUpdatedAt(LocalDateTime.now());
                    return notificationRepository.save(notification);
                })
                .map(this::toDto)
                .onErrorResume(error -> markFailed(notification, error));
    }

    private Mono<NotificationDto> sendSms(Notification notification) {
        return smsClient.send(
                        notification.getCustomerPhone(),
                        notification.getBody())
                .flatMap(providerId -> {
                    notification.setStatus("SENT");
                    notification.setProviderMessageId(providerId);
                    notification.setSentAt(LocalDateTime.now());
                    notification.setUpdatedAt(LocalDateTime.now());
                    return notificationRepository.save(notification);
                })
                .map(this::toDto)
                .onErrorResume(error -> markFailed(notification, error));
    }

    private Mono<NotificationDto> markFailed(Notification notification, Throwable error) {
        log.error("❌ Notification failed: {}", notification.getNotificationId(), error);

        notification.setStatus("FAILED");
        notification.setErrorMessage(error.getMessage());
        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setUpdatedAt(LocalDateTime.now());

        return notificationRepository.save(notification).map(this::toDto);
    }

    // ─── QUERY ───────────────────────────────────────────────────────

    @Override
    public Flux<NotificationDto> getCustomerNotifications(String customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .map(this::toDto);
    }

    @Override
    public Flux<NotificationDto> getCustomerNotificationsByCategory(String customerId, String category) {
        return notificationRepository.findByCustomerIdAndNotificationCategoryOrderByCreatedAtDesc(customerId, category)
                .map(this::toDto);
    }

    // ─── RETRY ───────────────────────────────────────────────────────

    @Override
    public Mono<Void> retryFailedNotifications() {
        log.info("🔄 Retrying failed notifications...");

        return notificationRepository.findByStatusAndRetryCountLessThan("FAILED", maxRetries)
                .flatMap(notification -> {
                    log.info("🔄 Retrying notification: {}", notification.getNotificationId());
                    return dispatchNotification(notification);
                })
                .then()
                .doOnSuccess(v -> log.info("✅ Retry cycle complete"));
    }

    // ─── MAPPER ──────────────────────────────────────────────────────

    private NotificationDto toDto(Notification notification) {
        return NotificationDto.builder()
                .notificationId(notification.getNotificationId())
                .customerId(notification.getCustomerId())
                .notificationType(notification.getNotificationType())
                .notificationCategory(notification.getNotificationCategory())
                .status(notification.getStatus())
                .subject(notification.getSubject())
                .body(notification.getBody())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
