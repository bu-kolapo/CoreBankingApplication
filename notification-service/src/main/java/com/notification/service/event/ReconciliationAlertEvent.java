package com.notification.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationAlertEvent {
    private String batchId;
    private String provider;
    private Long unmatchedCount;
    private Long missingInternalCount;
    private Long missingInGatewayCount;
    private String adminEmail;              // Alert goes to ops team, not customer
}