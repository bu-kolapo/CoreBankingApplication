package com.notification.service.service;

import com.commonlib.event.FraudAlertEvent;
import com.commonlib.event.PaymentEvent;
import com.commonlib.event.TransactionEvent;
import com.notification.service.dto.NotificationRequestDto;
import reactor.core.publisher.Mono;

public interface NotificationService  {

    /**
     * Send notification
     */
    Mono<Void> sendNotification(NotificationRequestDto request);

    /**
     * Send payment notification
     */
    Mono<Void> sendPaymentNotification(PaymentEvent event);

    /**
     * Send transaction notification
     */
    Mono<Void> sendTransactionNotification(TransactionEvent event);

    /**
     * Send fraud alert notification
     */
    Mono<Void> sendFraudAlertNotification(FraudAlertEvent event);
}


