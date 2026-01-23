package com.account.service.repository;

import com.account.service.model.Account;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Repository
public interface AccountRepository extends R2dbcRepository<Account, Long> {
    Mono<Account> findByAccountNumber(String accountNumber);
    Flux<Account> findByCustomerId(Long customerId);
    Flux<Account> findByStatus(String status);

    @Query("SELECT * FROM accounts WHERE customer_id = :customerId AND status = 'ACTIVE'")
    Flux<Account> findActiveAccountsByCustomerId(Long customerId);

    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :accountId RETURNING *")
    Mono<Account> creditAccount(Long accountId, BigDecimal amount);

    @Query("UPDATE accounts SET balance = balance - :amount WHERE id = :accountId AND balance >= :amount RETURNING *")
    Mono<Account> debitAccount(Long accountId, BigDecimal amount);
}
