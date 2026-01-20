package com.payments.service.repository;

// payments-app/src/main/java/com.bank.payments/repo/PaymentEntity.java
import com.payments.service.model.PaymentProvider;
import com.payments.service.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("payments")
public class PaymentEntity {
    @Id
    private Long id;
    private String paymentId; // Unique payment identifier
    private Long orderId;
    private Long accountId;
    private String paymentGateway; // STRIPE, PAYPAL, RAZORPAY
    private String gatewayTransactionId;
    private BigDecimal amount;
    private String currency;
    private String status; // INITIATED, PROCESSING, SUCCESS, FAILED, REFUNDED
    private String paymentMethod; // CARD, UPI, NET_BANKING, WALLET
    private String cardLast4;
    private String customerEmail;
    private String callbackUrl;
    private String webhookUrl;
    private Integer retryCount;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

