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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("reconciliation_batches")
public class ReconciliationBatch {
    @Id
    private Long id;

    private String batchId;                 // UUID
    private String batchName;               // e.g., "RECON-2026-02-03"
    private String provider;                // STRIPE, PAYPAL
    private String status;                  // INITIATED, PROCESSING, COMPLETED, FAILED
    private String reconciliationDate;      // The date being reconciled (yyyy-MM-dd)

    private Long totalGatewayRecords;       // How many records Stripe reported
    private Long totalInternalRecords;      // How many records we have internally
    private Long matchedCount;              // Records that matched perfectly
    private Long unmatchedCount;            // Records that did NOT match
    private Long missingInternalCount;      // In Stripe but NOT in our DB
    private Long missingInGatewayCount;     // In our DB but NOT in Stripe

    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}