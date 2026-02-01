package com.payments.service.service.Implementations;

import com.commonlib.response.PaymentResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.service.model.IdempotencyKey;
import com.payments.service.repository.IdempotencyKeyRepository;
import com.payments.service.service.IdempotencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@Slf4j
public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyKeyRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotencyServiceImpl(IdempotencyKeyRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public Mono<PaymentResponseDto> checkIdempotency(String key, String method, String endpoint) {
        if (key == null || key.isEmpty()) {
            return Mono.empty();
        }

        return repository.findByKeyAndEndpointAndMethod(key, endpoint, method)
                .filter(idem -> idem.getExpiresAt().isAfter(LocalDateTime.now()))
                .flatMap(idem -> {
                    try {
                        PaymentResponseDto response = objectMapper.readValue(
                                idem.getResponseBody(), PaymentResponseDto.class);
                        log.info("♻️ Idempotency cache hit for key: {}", key);
                        return Mono.just(response);
                    } catch (Exception e) {
                        log.error("❌ Failed to deserialize cached response", e);
                        return Mono.empty();
                    }
                });
    }

    @Override
    public Mono<Void> storeResponse(String key, String method, String endpoint, Object response, int statusCode) {
        try {
            String responseBody = objectMapper.writeValueAsString(response);

            // Generate a simple hash for the request (using the idempotency key as proxy)
            String requestHash = generateHash(key + method + endpoint);

            IdempotencyKey idempotencyKey = IdempotencyKey.builder()
                    .idempotencyKey(key)
                    .requestHash(requestHash)  // ← Added this
                    .endpoint(endpoint)
                    .httpMethod(method)
                    .responseBody(responseBody)
                    .responseStatus(statusCode)
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .createdAt(LocalDateTime.now())
                    .build();

            return repository.save(idempotencyKey)
                    .doOnSuccess(saved -> log.info("💾 Stored idempotency key: {}", key))
                    .then();
        } catch (Exception e) {
            log.error("❌ Failed to store idempotency key", e);
            return Mono.empty();
        }
    }

    // Add this helper method
    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash
            return String.valueOf(input.hashCode());
        }
    }
}
