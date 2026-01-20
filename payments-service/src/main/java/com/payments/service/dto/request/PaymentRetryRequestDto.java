package com.payments.service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRetryRequestDto {
    @NotNull
    private Long paymentId;

    private String reason;
    private LocalDateTime scheduledAt;
}