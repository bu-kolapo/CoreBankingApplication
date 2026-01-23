package com.notification.service.repository;

import com.notification.service.model.Notification;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface NotificationRepository extends R2dbcRepository<Notification, Long> {
    Flux<Notification> findByCustomerId(Long customerId);
    Flux<Notification> findByStatus(String status);

    @Query("SELECT * FROM notifications WHERE status = 'PENDING' " +
            "AND created_at <= :beforeTime ORDER BY created_at ASC LIMIT :batchSize")
    Flux<Notification> findPendingNotifications(LocalDateTime beforeTime, int batchSize);
}
