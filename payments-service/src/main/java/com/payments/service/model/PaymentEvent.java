package com.payments.service.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    private PaymentEventType type;
    private String paymentId;
    private String orderId;
    private String gatewayRef;
    private String payloadJson;
    private Instant timestamp;

    public static PaymentEvent confirmed(Payment p) {
        PaymentEvent e = new PaymentEvent();
        e.type = PaymentEventType.CONFIRMED;
        e.paymentId = p.getId().toString();
        e.orderId = p.getOrderId().toString();
        e.gatewayRef = p.getGatewayRef();
        e.timestamp = Instant.now();
        return e;
    }

    public static PaymentEvent failed(Payment p, Throwable ex) {
        PaymentEvent e = new PaymentEvent();
        e.type = PaymentEventType.FAILED;
        e.paymentId = p.getId().toString();
        e.orderId = p.getOrderId().toString();
        e.payloadJson = "{\"error\":\"" + ex.getMessage() + "\"}";
        e.timestamp = Instant.now();
        return e;
    }
}
