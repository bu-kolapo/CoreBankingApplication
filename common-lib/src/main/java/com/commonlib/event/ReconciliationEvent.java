package com.commonlib.event;

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
public class ReconciliationEvent {
    private String eventId;
    private String reconciliationId;
    private String paymentId;
    private String orderId;
    private String status; // MATCHED, UNMATCHED, DISCREPANCY
    private BigDecimal expectedAmount;
    private BigDecimal actualAmount;
    private LocalDateTime timestamp;
}
