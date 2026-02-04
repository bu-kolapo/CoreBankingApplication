package com.notification.service.service;

import com.commonlib.event.FraudAlertEvent;
import com.commonlib.event.PaymentEvent;
import com.commonlib.event.TransactionEvent;
import com.notification.service.dto.NotificationDto;
import com.notification.service.dto.SendNotificationRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationService  {

    // Core: create and send a notification
    Mono<NotificationDto> sendNotification(SendNotificationRequest request);

    // Query: get all notifications for a customer
    Flux<NotificationDto> getCustomerNotifications(String customerId);

    // Query: get by category
    Flux<NotificationDto> getCustomerNotificationsByCategory(String customerId, String category);

    // Retry: pick up FAILED notifications and try again
    Mono<Void> retryFailedNotifications();
}


