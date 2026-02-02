package com.account.service.service;

import com.account.service.dto.request.AccountRequest;
import com.account.service.dto.request.CreditAccountRequest;
import com.account.service.dto.request.DebitAccountRequest;
import com.commonlib.dto.AccountDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AccountService {
 // Account creation
 Mono<AccountDto> createAccount(AccountRequest request);

 // Account retrieval
 Mono<AccountDto> getAccount(String accountId);

 Mono<AccountDto> getAccountByNumber(String accountNumber);

 Flux<AccountDto> getCustomerAccounts(String customerId);

 Mono<AccountDto> getPrimaryAccount(String customerId);

 // Balance operations
 Mono<AccountDto> debitAccount(DebitAccountRequest request);

 Mono<AccountDto> creditAccount(CreditAccountRequest request);

 Mono<BigDecimal> getBalance(String accountId);

 Mono<BigDecimal> getAvailableBalance(String accountId);

 // Balance holds
 Mono<Void> holdFunds(String accountId, BigDecimal amount, String reference);

 Mono<Void> releaseFunds(String accountId, BigDecimal amount, String reference);

 // Account management
 Mono<Void> freezeAccount(String accountId, String reason);

 Mono<Void> unfreezeAccount(String accountId);

 Mono<Void> closeAccount(String accountId);

 // Validation
 Mono<Boolean> hasSufficientBalance(String accountId, BigDecimal amount);

 Mono<Boolean> isWithinDailyLimit(String accountId, BigDecimal amount);
}
