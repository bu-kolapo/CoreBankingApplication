package com.fraud.etection.service.repository;

import com.fraud.etection.service.model.CustomerRiskProfile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRiskProfileRepository extends ReactiveCrudRepository<CustomerRiskProfile, Long> {

    Mono<CustomerRiskProfile> findByCustomerId(String customerId);

    Flux<CustomerRiskProfile> findByRiskRatingOrderByRiskScoreDesc(String riskRating);

    Flux<CustomerRiskProfile> findByBlacklistedTrue();

    Flux<CustomerRiskProfile> findByWhitelistedTrue();
}