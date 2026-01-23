package com.transaction.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("transactions")
public class Transaction {
    @Id
    private Long id;
    private String transactionId; // Unique transaction identifier
    private String transactionType; // DEBIT, CREDIT, TRANSFER
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private String currency;
    private String status; // PENDING, COMPLETED, FAILED, REVERSED
    private String description;
    private String category;
    private LocalDateTime transactionDate;
    private String referenceNumber;
    private String initiatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}