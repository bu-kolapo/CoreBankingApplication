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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ledger_entries")
public class LedgerEntry {

    @Id
    private Long id;

    private String entryId;                 // UUID
    private String transactionId;           // Link to transaction

    private String accountId;               // Account affected
    private String accountType;             // CUSTOMER, MERCHANT, BANK, GATEWAY

    private String entryType;               // DEBIT, CREDIT
    private BigDecimal amount;
    private String currency;

    private BigDecimal balanceBefore;       // Account balance before transaction
    private BigDecimal balanceAfter;        // Account balance after transaction

    private String description;
    private String referenceNumber;

    private LocalDateTime entryDate;
    private LocalDateTime createdAt;
}