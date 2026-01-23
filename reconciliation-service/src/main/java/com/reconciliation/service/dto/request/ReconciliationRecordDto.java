package com.reconciliation.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReconciliationRecordDto {
    private String reconciliationId;
    private Long paymentId;
    private Long orderId;
    private String status;
    private BigDecimal expectedAmount;
    private BigDecimal actualAmount;
    private String discrepancyReason;
}
