package com.orders.service.events.consumer;

import com.orders.service.events.PaymentCompletedEvent;
import com.orders.service.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventConsumer {

    private final OrderService orderService;

    public PaymentEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "payment-completed", groupId = "order-service-group")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("🎧 [OrderService] Payment completed for order: {}", event.getOrderId());

        orderService.handlePaymentCompleted(event)
                .doOnSuccess(order -> log.info("✅ Order updated: {}", order.getOrderId()))
                .doOnError(error -> log.error("❌ Failed to update order from payment event", error))
                .subscribe();
    }
}