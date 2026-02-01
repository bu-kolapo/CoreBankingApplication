package com.webhook.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallbackVerificationDto {
    private String signature;
    private String timestamp;
    private String payload;
    private boolean verified;
}