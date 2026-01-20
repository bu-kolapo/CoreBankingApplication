package com.payments.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyResponseDto {
    private String idempotencyKey;
    private boolean cached;
    private Object cachedResponse;
    private LocalDateTime cachedAt;
}