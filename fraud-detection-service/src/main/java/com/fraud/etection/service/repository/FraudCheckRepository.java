package com.fraud.etection.service.repository;

import com.fraud.etection.service.model.FraudCheck;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface FraudCheckRepository extends ReactiveCrudRepository<FraudCheck, Long> {

    Mono<FraudCheck> findByCheckId(String checkId);

    Flux<FraudCheck> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    Flux<FraudCheck> findByEntityTypeAndEntityId(String entityType, String entityId);

    Flux<FraudCheck> findByDecisionOrderByCreatedAtDesc(String decision);

    Flux<FraudCheck> findByReviewStatusOrderByCreatedAtDesc(String reviewStatus);

    @Query("SELECT * FROM fraud_checks WHERE customer_id = :customerId AND created_at >= :since")
    Flux<FraudCheck> findRecentByCustomer(String customerId, LocalDateTime since);

    @Query("SELECT COUNT(*) FROM fraud_checks WHERE customer_id = :customerId AND decision = :decision AND created_at >= :since")
    Mono<Long> countByCustomerAndDecisionSince(String customerId, String decision, LocalDateTime since);
}