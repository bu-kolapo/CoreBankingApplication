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

    private String transactionId;           // UUID - our internal ID
    private String paymentId;               // Link to payment
    private String orderId;                 // Link to order
    private String accountId;               // Customer account

    private String transactionType;         // PAYMENT, REFUND, REVERSAL, FEE
    private String transactionCategory;     // DEBIT, CREDIT
    private BigDecimal amount;
    private String currency;

    private String status;                  // PENDING, COMPLETED, FAILED, REVERSED
    private String description;

    // Gateway information
    private String gatewayTransactionId;    // Stripe/PayPal transaction ID
    private String paymentGateway;          // STRIPE, PAYPAL

    // Ledger information (Double-entry bookkeeping)
    private String debitAccountId;          // Source account
    private String creditAccountId;         // Destination account

    // Reconciliation
    private Boolean reconciled;
    private LocalDateTime reconciledAt;
    private String reconciliationBatchId;

    // Metadata
    private String referenceNumber;         // Customer-facing reference
    private String metadata;                // JSON for additional data

    // Audit fields
    private LocalDateTime transactionDate;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}