package com.customer.service.repository;

import com.customer.service.model.CustomerAccount;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerAccountRepository extends ReactiveCrudRepository<CustomerAccount, Long> {

    Mono<CustomerAccount> findByAccountId(String accountId);

    Mono<CustomerAccount> findByAccountNumber(String accountNumber);

    Flux<CustomerAccount> findByCustomerId(String customerId);

    Flux<CustomerAccount> findByCustomerIdAndStatus(String customerId, String status);

    Mono<CustomerAccount> findByCustomerIdAndAccountType(String customerId, String accountType);
}