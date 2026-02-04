package com.reconciliation.service.model;


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
@Table("reconciliation_records")
public class ReconciliationRecord {

    @Id
    private Long id;

    private String recordId;                // UUID
    private String batchId;                 // Links to reconciliation_batches

    // Internal side (our records)
    private String internalTransactionId;   // From Transaction Service
    private String internalPaymentId;       // From Payment Service
    private BigDecimal internalAmount;
    private String internalStatus;          // COMPLETED, FAILED, REFUNDED

    // Gateway side (Stripe records)
    private String gatewayTransactionId;    // Stripe's pi_xxx or ch_xxx
    private BigDecimal gatewayAmount;
    private String gatewayStatus;           // succeeded, failed, refunded
    private String gatewaySettledAt;

    // Reconciliation result
    private String reconciliationStatus;    // MATCHED, AMOUNT_MISMATCH, STATUS_MISMATCH,
    // MISSING_INTERNAL, MISSING_IN_GATEWAY, DUPLICATE
    private String discrepancyDetails;      // JSON describing what exactly didn't match
    private Boolean requiresManualReview;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
