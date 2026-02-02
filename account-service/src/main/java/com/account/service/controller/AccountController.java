package com.account.service.controller;


import com.account.service.dto.request.AccountRequest;
import com.account.service.dto.request.CreditAccountRequest;
import com.account.service.dto.request.DebitAccountRequest;
import com.account.service.dto.response.BalanceResponse;
import com.account.service.dto.response.ValidationResponse;
import com.account.service.service.AccountService;
import com.commonlib.dto.AccountDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Create a new account for a customer
     */
    @PostMapping
    public Mono<ResponseEntity<AccountDto>> createAccount(
            @Valid @RequestBody AccountRequest request) {

        log.info("📝 Received create account request for customer: {}", request.getCustomerId());

        return accountService.createAccount(request)
                .map(account -> ResponseEntity.status(HttpStatus.CREATED).body(account))
                .onErrorResume(error -> {
                    log.error("❌ Failed to create account", error);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    /**
     * Get account by account ID
     */
    @GetMapping("/{accountId}")
    public Mono<ResponseEntity<AccountDto>> getAccount(@PathVariable String accountId) {
        log.info("🔍 Getting account: {}", accountId);

        return accountService.getAccount(accountId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    log.error("❌ Failed to get account", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Get account by account number
     */
    @GetMapping("/number/{accountNumber}")
    public Mono<ResponseEntity<AccountDto>> getAccountByNumber(@PathVariable String accountNumber) {
        log.info("🔍 Getting account by number: {}", accountNumber);

        return accountService.getAccountByNumber(accountNumber)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    log.error("❌ Failed to get account", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Get all accounts for a customer
     */
    @GetMapping("/customer/{customerId}")
    public Flux<AccountDto> getCustomerAccounts(@PathVariable String customerId) {
        log.info("🔍 Getting accounts for customer: {}", customerId);

        return accountService.getCustomerAccounts(customerId)
                .doOnComplete(() -> log.info("✅ Retrieved accounts for customer: {}", customerId))
                .onErrorResume(error -> {
                    log.error("❌ Failed to get customer accounts", error);
                    return Flux.empty();
                });
    }

    /**
     * Get primary account for a customer
     */
    @GetMapping("/customer/{customerId}/primary")
    public Mono<ResponseEntity<AccountDto>> getPrimaryAccount(@PathVariable String customerId) {
        log.info("🔍 Getting primary account for customer: {}", customerId);

        return accountService.getPrimaryAccount(customerId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    log.error("❌ Failed to get primary account", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Debit an account (withdraw funds)
     */
    @PostMapping("/debit")
    public Mono<ResponseEntity<AccountDto>> debitAccount(
            @Valid @RequestBody DebitAccountRequest request) {

        log.info("💸 Received debit request for account: {} amount: {}",
                request.getAccountId(), request.getAmount());

        return accountService.debitAccount(request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("❌ Failed to debit account", error);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    /**
     * Credit an account (deposit funds)
     */
    @PostMapping("/credit")
    public Mono<ResponseEntity<AccountDto>> creditAccount(
            @Valid @RequestBody CreditAccountRequest request) {

        log.info("💰 Received credit request for account: {} amount: {}",
                request.getAccountId(), request.getAmount());

        return accountService.creditAccount(request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("❌ Failed to credit account", error);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    /**
     * Get account balance
     */
    @GetMapping("/{accountId}/balance")
    public Mono<ResponseEntity<BalanceResponse>> getBalance(@PathVariable String accountId) {
        log.info("🔍 Getting balance for account: {}", accountId);

        return accountService.getBalance(accountId)
                .zipWith(accountService.getAvailableBalance(accountId))
                .map(tuple -> {
                    BigDecimal balance = tuple.getT1();
                    BigDecimal availableBalance = tuple.getT2();

                    return ResponseEntity.ok(BalanceResponse.builder()
                            .accountId(accountId)
                            .balance(balance)
                            .availableBalance(availableBalance)
                            .build());
                })
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    log.error("❌ Failed to get balance", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Get available balance
     */
    @GetMapping("/{accountId}/available-balance")
    public Mono<ResponseEntity<BigDecimal>> getAvailableBalance(@PathVariable String accountId) {
        log.info("🔍 Getting available balance for account: {}", accountId);

        return accountService.getAvailableBalance(accountId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    log.error("❌ Failed to get available balance", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Hold funds on an account
     */
    @PostMapping("/{accountId}/hold")
    public Mono<ResponseEntity<Void>> holdFunds(
            @PathVariable String accountId,
            @RequestParam BigDecimal amount,
            @RequestParam String reference) {

        log.info("🔒 Holding funds on account: {} amount: {}", accountId, amount);

        return accountService.holdFunds(accountId, amount, reference)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(error -> {
                    log.error("❌ Failed to hold funds", error);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    /**
     * Release held funds on an account
     */
    @PostMapping("/{accountId}/release")
    public Mono<ResponseEntity<Void>> releaseFunds(
            @PathVariable String accountId,
            @RequestParam BigDecimal amount,
            @RequestParam String reference) {

        log.info("🔓 Releasing held funds on account: {} amount: {}", accountId, amount);

        return accountService.releaseFunds(accountId, amount, reference)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(error -> {
                    log.error("❌ Failed to release funds", error);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    /**
     * Freeze an account
     */
    @PostMapping("/{accountId}/freeze")
    public Mono<ResponseEntity<Void>> freezeAccount(
            @PathVariable String accountId,
            @RequestParam String reason) {

        log.info("❄️ Freezing account: {} reason: {}", accountId, reason);

        return accountService.freezeAccount(accountId, reason)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(error -> {
                    log.error("❌ Failed to freeze account", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Unfreeze an account
     */
    @PostMapping("/{accountId}/unfreeze")
    public Mono<ResponseEntity<Void>> unfreezeAccount(@PathVariable String accountId) {
        log.info("✅ Unfreezing account: {}", accountId);

        return accountService.unfreezeAccount(accountId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(error -> {
                    log.error("❌ Failed to unfreeze account", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Close an account
     */
    @PostMapping("/{accountId}/close")
    public Mono<ResponseEntity<Void>> closeAccount(@PathVariable String accountId) {
        log.info("🔒 Closing account: {}", accountId);

        return accountService.closeAccount(accountId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(error -> {
                    log.error("❌ Failed to close account", error);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    /**
     * Check if account has sufficient balance
     */
    @GetMapping("/{accountId}/validate/sufficient-balance")
    public Mono<ResponseEntity<ValidationResponse>> checkSufficientBalance(
            @PathVariable String accountId,
            @RequestParam BigDecimal amount) {

        log.info("🔍 Checking sufficient balance for account: {} amount: {}", accountId, amount);

        return accountService.hasSufficientBalance(accountId, amount)
                .map(hasSufficientBalance -> ResponseEntity.ok(ValidationResponse.builder()
                        .valid(hasSufficientBalance)
                        .message(hasSufficientBalance ?
                                "Sufficient balance available" :
                                "Insufficient balance")
                        .build()))
                .defaultIfEmpty(ResponseEntity.ok(ValidationResponse.builder()
                        .valid(false)
                        .message("Account not found")
                        .build()));
    }

    /**
     * Check if transaction is within daily limit
     */
    @GetMapping("/{accountId}/validate/daily-limit")
    public Mono<ResponseEntity<ValidationResponse>> checkDailyLimit(
            @PathVariable String accountId,
            @RequestParam BigDecimal amount) {

        log.info("🔍 Checking daily limit for account: {} amount: {}", accountId, amount);

        return accountService.isWithinDailyLimit(accountId, amount)
                .map(withinLimit -> ResponseEntity.ok(ValidationResponse.builder()
                        .valid(withinLimit)
                        .message(withinLimit ?
                                "Within daily transaction limit" :
                                "Exceeds daily transaction limit")
                        .build()))
                .defaultIfEmpty(ResponseEntity.ok(ValidationResponse.builder()
                        .valid(false)
                        .message("Account not found")
                        .build()));
    }
}
