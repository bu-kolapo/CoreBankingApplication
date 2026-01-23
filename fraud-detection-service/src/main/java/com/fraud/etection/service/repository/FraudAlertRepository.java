package com.fraud.etection.service.repository;

import com.fraud.etection.service.model.FraudAlert;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface FraudAlertRepository extends R2dbcRepository<FraudAlert, Long> {
    Flux<FraudAlert> findByTransactionId(Long transactionId);
    Flux<FraudAlert> findByAccountId(Long accountId);
    Flux<FraudAlert> findByStatus(String status);

    @Query("SELECT * FROM fraud_alerts WHERE severity IN ('HIGH', 'CRITICAL') " +
            "AND status = 'OPEN' ORDER BY created_at DESC")
    Flux<FraudAlert> findHighPriorityOpenAlerts();
}