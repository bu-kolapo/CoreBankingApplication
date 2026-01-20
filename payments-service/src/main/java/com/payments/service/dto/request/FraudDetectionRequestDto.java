package com.payments.service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudDetectionRequestDto {
    @NotNull
    private Long transactionId;

    @NotNull
    private Long accountId;

    @NotNull
    private BigDecimal amount;

    private String location;
    private String ipAddress;
    private String deviceId;
}