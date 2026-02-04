package com.notification.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendNotificationRequest {


    private String customerId;
    private String customerEmail;
    private String customerPhone;
    private String notificationType;        // EMAIL, SMS, PUSH
    private String notificationCategory;
    private String subject;
    private String body;
    private String templateId;
    private String sourceEventType;
    private String sourceEntityId;
    private String sourceEntityType;
}
