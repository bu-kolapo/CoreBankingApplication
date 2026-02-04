package com.notification.service.repository;

import com.notification.service.model.Notification;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface NotificationRepository extends ReactiveCrudRepository<Notification, Long> {
    Mono<Notification> findByNotificationId(String notificationId);

    Flux<Notification> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    Flux<Notification> findByCustomerIdAndNotificationCategoryOrderByCreatedAtDesc(
            String customerId, String category);

    Flux<Notification> findByStatusAndRetryCountLessThan(String status, int maxRetries);

    @Query("SELECT * FROM notifications WHERE customer_id = :customerId AND created_at BETWEEN :start AND :end ORDER BY created_at DESC")
    Flux<Notification> findByCustomerAndDateRange(String customerId, LocalDateTime start, LocalDateTime end);

}
