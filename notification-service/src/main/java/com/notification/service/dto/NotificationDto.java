package com.notification.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {

    private String notificationId;
    private String customerId;
    private String notificationType;
    private String notificationCategory;
    private String status;
    private String subject;
    private String body;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
