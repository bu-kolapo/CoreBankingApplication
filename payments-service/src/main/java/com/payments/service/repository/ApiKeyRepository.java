package com.payments.service.repository;


import com.payments.service.model.ApiKey;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface ApiKeyRepository extends R2dbcRepository<ApiKey, Long> {
    Mono<ApiKey> findByKeyHash(String keyHash);
    Flux<ApiKey> findByCustomerId(Long customerId);
    Flux<ApiKey> findByStatus(String status);

    @Query("UPDATE api_keys SET last_used_at = :lastUsedAt WHERE key_hash = :keyHash RETURNING *")
    Mono<ApiKey> updateLastUsedAt(String keyHash, LocalDateTime lastUsedAt);
}