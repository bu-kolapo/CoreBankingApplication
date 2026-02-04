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
public class ReconciliationRecordDto {

    private String recordId;
    private String batchId;
    private String internalTransactionId;
    private String internalPaymentId;
    private BigDecimal internalAmount;
    private String internalStatus;
    private String gatewayTransactionId;
    private BigDecimal gatewayAmount;
    private String gatewayStatus;
    private String reconciliationStatus;
    private String discrepancyDetails;
    private Boolean requiresManualReview;
    private LocalDateTime createdAt;
}
