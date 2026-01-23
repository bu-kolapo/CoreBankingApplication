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
public class WebhookEventDto {
    private String eventId;
    private String eventType;
    private String source;
    private String paymentId;
    private String payload;
    private String signature;
    private LocalDateTime timestamp;
}