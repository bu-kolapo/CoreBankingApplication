package com.account.service.model;

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
@Table("accounts")
public class Account {
    @Id
    private Long id;

    private String accountId;               // UUID
    private String accountNumber;           // ACC-000001
    private Long customerId;              // Reference to customer

    private String accountType;             // SAVINGS, CHECKING, WALLET
    private String currency;

    // Balances
    private BigDecimal balance;             // Current balance
    private BigDecimal availableBalance;    // Balance available for use
    private BigDecimal blockedAmount;       // Held/frozen funds
    private BigDecimal pendingDebit;        // Pending debits
    private BigDecimal pendingCredit;       // Pending credits

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