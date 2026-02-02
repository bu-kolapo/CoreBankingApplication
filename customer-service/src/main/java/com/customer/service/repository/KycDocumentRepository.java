package com.customer.service.repository;

import com.customer.service.model.KycDocument;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface KycDocumentRepository extends ReactiveCrudRepository<KycDocument, Long> {

    Mono<KycDocument> findByDocumentId(String documentId);

    Flux<KycDocument> findByCustomerId(String customerId);

    Flux<KycDocument> findByCustomerIdAndVerificationStatus(String customerId, String status);

    Flux<KycDocument> findByVerificationStatus(String status);
}