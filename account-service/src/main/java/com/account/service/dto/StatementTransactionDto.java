package com.account.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementTransactionDto {

    private String transactionId;
    private String referenceNumber;
    private String transactionType;         // PAYMENT, REFUND, REVERSAL, FEE
    private String transactionCategory;     // DEBIT, CREDIT
    private BigDecimal amount;
    private String currency;
    private BigDecimal runningBalance;      // Balance after this transaction
    private String description;
    private LocalDateTime transactionDate;
}