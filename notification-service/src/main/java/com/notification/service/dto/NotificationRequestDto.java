package com.notification.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequestDto {

    private Long customerId;
    private String type;
    private String subject;
    private String message;
    private String channel;
}
