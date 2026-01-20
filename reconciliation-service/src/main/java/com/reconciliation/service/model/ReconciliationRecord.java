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
@Table("reconciliation_records")
public class ReconciliationRecord {
    @Id
    private Long id;
    private String reconciliationId;
    private Long paymentId;
    private Long orderId;
    private Long transactionId;
    private String status; // MATCHED, UNMATCHED, DISCREPANCY
    private String discrepancyType;
    private String discrepancyReason;
    private BigDecimal expectedAmount;
    private BigDecimal actualAmount;
    private String reconciledBy;
    private LocalDateTime reconciledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}