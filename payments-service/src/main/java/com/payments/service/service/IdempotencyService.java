package com.payments.service.service;

import com.commonlib.response.PaymentResponseDto;
import reactor.core.publisher.Mono;

public interface IdempotencyService {
    Mono<PaymentResponseDto> checkIdempotency(String key, String method, String endpoint);
    Mono<Void> storeResponse(String key, String method, String endpoint, Object response, int statusCode);
}
