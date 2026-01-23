package com.commonlib.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {
    private String notificationId;
    private Long customerId;
    private String type; // EMAIL, SMS, PUSH
    private String subject;
    private String message;
    private String referenceId;
    private String referenceType;
    private LocalDateTime timestamp;
}