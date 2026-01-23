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
    private Long customerId;
    private String notificationType; // EMAIL, SMS, PUSH
    private String channel;
    private String subject;
    private String message;
    private String status; // PENDING, SENT, FAILED
    private String referenceId;
    private String referenceType;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
