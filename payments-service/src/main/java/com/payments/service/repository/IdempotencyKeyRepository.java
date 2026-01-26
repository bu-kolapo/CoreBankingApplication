package com.payments.service.repository;


import com.payments.service.model.IdempotencyKey;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface IdempotencyKeyRepository extends R2dbcRepository<IdempotencyKey, Long> {
    Mono<IdempotencyKey> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT * FROM idempotency_keys WHERE idempotency_key = :key " +
            "AND endpoint = :endpoint AND http_method = :method")
    Mono<IdempotencyKey> findByKeyAndEndpointAndMethod(String key, String endpoint, String method);

    @Query("DELETE FROM idempotency_keys WHERE expires_at < :expiryDate")
    Mono<Long> deleteExpiredKeys(LocalDateTime expiryDate);
}