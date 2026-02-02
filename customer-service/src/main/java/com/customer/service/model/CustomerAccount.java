package com.customer.service.model;

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
@Table("customer_accounts")
public class CustomerAccount {

    @Id
    private Long id;

    private String accountId;               // UUID
    private String accountNumber;           // Customer-facing account number
    private String customerId;              // Link to customer

    private String accountType;             // SAVINGS, CHECKING, WALLET, MERCHANT
    private String currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;    // Balance minus holds/pending
    private BigDecimal blockedAmount;       // Frozen/held funds

    private String status;                  // ACTIVE, INACTIVE, FROZEN, CLOSED

    // Limits
    private BigDecimal dailyTransactionLimit;
    private BigDecimal monthlyTransactionLimit;
    private BigDecimal minimumBalance;
    private BigDecimal overdraftLimit;

    // Interest (for savings accounts)
    private BigDecimal interestRate;
    private LocalDateTime lastInterestDate;

    // Metadata
    private String accountPurpose;          // PERSONAL, BUSINESS
    private String metadata;                // JSON

    // Audit
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
