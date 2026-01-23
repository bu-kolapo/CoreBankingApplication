package com.transaction.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {

    private Long id;
    private String transactionId;
    private String transactionType;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String description;
    private String category;
    private String referenceNumber;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
}
