package com.webhook.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponseDto {
    private String webhookId;
    private String eventId;
    private String status;
    private String message;
    private boolean processed;
}