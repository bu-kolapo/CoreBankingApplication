package com.payments.service.repository;

// payments-app/src/main/java/com.bank.payments/repo/PaymentRepository.java
import com.payments.service.model.Payment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface PaymentRepository extends ReactiveCrudRepository<PaymentEntity, UUID> {
    Mono<Payment> findByIdempotencyKey(String idempotencyKey);
    Mono<Payment> findByGatewayRef(String gatewayRef);
    Mono<Payment> findById(UUID id);
}
