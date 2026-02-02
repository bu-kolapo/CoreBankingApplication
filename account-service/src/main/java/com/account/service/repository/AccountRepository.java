package com.account.service.repository;

import com.account.service.model.Account;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {

    Mono<Account> findByAccountId(String accountId);

    Mono<Account> findByAccountNumber(String accountNumber);

    Flux<Account> findByCustomerId(String customerId);

    Flux<Account> findByCustomerIdAndStatus(String customerId, String status);

    Mono<Account> findByCustomerIdAndAccountTypeAndCurrency(
            String customerId, String accountType, String currency);

    @Query("SELECT * FROM accounts WHERE customer_id = :customerId AND status = 'ACTIVE' LIMIT 1")
    Mono<Account> findPrimaryAccountByCustomer(String customerId);
}