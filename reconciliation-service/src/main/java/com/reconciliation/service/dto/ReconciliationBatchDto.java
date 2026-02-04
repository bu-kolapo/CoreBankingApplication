package com.reconciliation.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationBatchDto {

    private String batchId;
    private String batchName;
    private String provider;
    private String status;
    private String reconciliationDate;
    private Long totalGatewayRecords;
    private Long totalInternalRecords;
    private Long matchedCount;
    private Long unmatchedCount;
    private Long missingInternalCount;
    private Long missingInGatewayCount;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
