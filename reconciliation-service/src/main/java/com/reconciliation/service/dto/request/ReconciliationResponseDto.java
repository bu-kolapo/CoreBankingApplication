package com.reconciliation.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationResponseDto {
    private String reconciliationId;
    private Integer totalRecords;
    private Integer matchedRecords;
    private Integer unmatchedRecords;
    private Integer discrepancyRecords;
    private BigDecimal totalAmount;
    private BigDecimal matchedAmount;
    private BigDecimal discrepancyAmount;
    private LocalDateTime reconciledAt;
}
