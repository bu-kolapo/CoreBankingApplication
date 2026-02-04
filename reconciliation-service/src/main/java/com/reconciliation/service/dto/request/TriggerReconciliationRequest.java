package com.reconciliation.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggerReconciliationRequest {

    @NotBlank(message = "Provider is required")
    private String provider;                // STRIPE, PAYPAL

    @NotBlank(message = "Reconciliation date is required (yyyy-MM-dd)")
    private String reconciliationDate;      // The DATE to reconcile, e.g. "2026-02-03"
}
