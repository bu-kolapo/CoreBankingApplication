package com.reconciliation.service.scheduler;

import com.reconciliation.service.dto.request.TriggerReconciliationRequest;
import com.reconciliation.service.service.ReconciliationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class ReconciliationScheduler {

    private final ReconciliationService reconciliationService;

    public ReconciliationScheduler(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    /**
     * Runs every day at 2:00 AM
     * Reconciles YESTERDAY's transactions (gives Stripe time to settle)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void runDailyReconciliation() {
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);

        log.info("⏰ Scheduled reconciliation triggered for date: {}", yesterday);

        TriggerReconciliationRequest request = TriggerReconciliationRequest.builder()
                .provider("STRIPE")
                .reconciliationDate(yesterday)
                .build();

        reconciliationService.triggerReconciliation(request)
                .doOnSuccess(batch -> log.info("✅ Scheduled reconciliation completed: {}", batch.getBatchId()))
                .doOnError(error -> log.error("❌ Scheduled reconciliation failed", error))
                .subscribe();
    }
}