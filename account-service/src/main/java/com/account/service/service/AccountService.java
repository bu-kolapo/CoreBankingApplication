package com.account.service.service;

import com.commonlib.dto.AccountDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AccountService {

   /**
            * Get account by ID
     */
    Mono<AccountDto> getAccountById(Long accountId);

    /**
     * Get account by account number
     */
    Mono<AccountDto> getAccountByNumber(String accountNumber);

    /**
     * Get all accounts for a customer
     */
    Flux<AccountDto> getAccountsByCustomerId(Long customerId);

    /**
     * Debit amount from account
     */
    Mono<AccountDto> debitAccount(Long accountId, BigDecimal amount);

    /**
     * Credit amount to account
     */
    Mono<AccountDto> creditAccount(Long accountId, BigDecimal amount);

    /**
     * Check account balance
     */
    Mono<BigDecimal> getAccountBalance(Long accountId);

    /**
     * Validate account status
     */
    Mono<Boolean> isAccountActive(Long accountId);


}
