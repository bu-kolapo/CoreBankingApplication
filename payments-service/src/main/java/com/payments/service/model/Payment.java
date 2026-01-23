package com.payments.service.model;

// payments-domain/src/main/java/com.bank.payments/model/Payment.java
import com.payments.service.dto.request.PaymentDto;
import com.payments.service.dto.response.GatewayChargeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    private UUID id;
    private UUID orderId;
    private Money amount;
    private String customerEmail;
    private PaymentStatus status;
    private String gatewayRef;       // external reference
    private String idempotencyKey;   // unique per request
    private String failureReason;    // nullable
    private Instant createdAt;
    private Instant updatedAt;

    public static Payment newPending(PaymentDto req, String idemKey) {
        Payment p = new Payment();
        p.id = UUID.randomUUID();
        p.orderId = req.getOrderId();
        p.amount = req.getAmount();
        p.customerEmail = req.getCustomerEmail();
        p.status = PaymentStatus.PENDING;
        p.idempotencyKey = idemKey;
        p.createdAt = Instant.now();
        p.updatedAt = p.createdAt;
        return p;
    }

    public Payment applyGatewayResponse(GatewayChargeResponse resp) {
        this.gatewayRef = resp.getGatewayRef();
        this.status = resp.isAuthorized() ? PaymentStatus.AUTHORIZED :
                resp.isConfirmed()  ? PaymentStatus.CONFIRMED : this.status;
        this.updatedAt = Instant.now();
        return this;
    }

    public Payment fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
        return this;
    }

}
