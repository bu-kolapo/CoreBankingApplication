package com.notification.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("notifications")
public class Notification {
    @Id
    private Long id;

    private String notificationId;          // UUID
    private String customerId;              // Who receives this
    private String customerEmail;
    private String customerPhone;

    private String notificationType;        // EMAIL, SMS, PUSH
    private String notificationCategory;    // PAYMENT, TRANSACTION, ACCOUNT, KYC, SECURITY, RECONCILIATION
    private String status;                  // PENDING, SENT, FAILED, DELIVERED

    private String subject;                 // Email subject / push title
    private String body;                    // The actual message content
    private String templateId;              // Which template was used

    // Source event tracking — what triggered this notification
    private String sourceEventType;         // e.g., "payment-completed"
    private String sourceEntityId;          // e.g., the paymentId or transactionId
    private String sourceEntityType;        // PAYMENT, TRANSACTION, ACCOUNT, etc.

    // Provider tracking
    private String providerMessageId;       // ID returned by email/SMS provider
    private String errorMessage;            // What went wrong if FAILED
    private Integer retryCount;

    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
