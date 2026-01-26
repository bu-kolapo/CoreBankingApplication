package com.commonlib.messaging;

import com.commonlib.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;


    // Exchange
    private static final String PAYMENT_EXCHANGE = "payment.exchange";

    // Routing keys
    private static final String PAYMENT_CREATED_ROUTING_KEY = "payment.created";
    private static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";
    private static final String PAYMENT_FAILED_ROUTING_KEY  = "payment.failed";
    private static final String RECONCILIATION_ROUTING_KEY  = "payment.reconciliation";


    public void publishPaymentSuccess(PaymentEvent event) {
        rabbitTemplate.convertAndSend(
                PAYMENT_EXCHANGE,
                PAYMENT_SUCCESS_ROUTING_KEY,
                event
        );

        log.info(
                "Payment success event published. paymentId={}, eventId={}",
                event.getPaymentId(),
                event.getEventId()
        );
    }
    public void publishReconciliationRequest(PaymentEvent event) {
        rabbitTemplate.convertAndSend(
                PAYMENT_EXCHANGE,
                RECONCILIATION_ROUTING_KEY,
                event
        );

        log.info(
                "Reconciliation event published. paymentId={}",
                event.getPaymentId()
        );
    }


    public void publishPaymentCreated(PaymentEvent event) {

        rabbitTemplate.convertAndSend(
                PAYMENT_EXCHANGE,
                PAYMENT_CREATED_ROUTING_KEY,
                event
        );

        log.info(
                "Payment created event published. paymentId={}, eventId={}",
                event.getPaymentId(),
                event.getEventId()
        );
    }

    public void publishPaymentFailed(PaymentEvent event) {

        rabbitTemplate.convertAndSend(
                PAYMENT_EXCHANGE,
                PAYMENT_FAILED_ROUTING_KEY,
                event
        );

        log.warn(
                "Payment failed event published. paymentId={}, reason={}",
                event.getPaymentId(),
                event.getFailureReason()
        );
    }


}


