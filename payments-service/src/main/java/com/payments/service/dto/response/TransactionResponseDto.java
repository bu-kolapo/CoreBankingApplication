package com.payments.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDto {
    private String transactionId;
    private String transactionType;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String referenceNumber;
    private LocalDateTime transactionDate;
}