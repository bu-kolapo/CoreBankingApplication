package com.account.service.service.Implementation;
import com.account.service.clients.TransactionServiceClient;
import com.account.service.dto.AccountStatementDto;
import com.account.service.dto.StatementTransactionDto;
import com.account.service.dto.request.AccountRequest;
import com.account.service.dto.request.CreditAccountRequest;
import com.account.service.dto.request.DebitAccountRequest;
import com.account.service.model.Account;
import com.account.service.repository.AccountRepository;
import com.account.service.service.AccountService;
import com.commonlib.dto.AccountDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TransactionServiceClient transactionServiceClient;

    public AccountServiceImpl(AccountRepository accountRepository, KafkaTemplate<String, Object> kafkaTemplate,TransactionServiceClient transactionServiceClient) {
        this.accountRepository = accountRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.transactionServiceClient=transactionServiceClient;
    }

    @Override
    @Transactional
    public Mono<AccountDto> createAccount(AccountRequest request) {
        log.info("💳 Creating account for customer: {}", request.getCustomerId());

        String accountId = UUID.randomUUID().toString();
        String accountNumber = generateAccountNumber();
        BigDecimal initialBalance = request.getInitialDeposit() != null ?
                request.getInitialDeposit() : BigDecimal.ZERO;

        Account account = Account.builder()
                .accountId(accountId)
                .accountNumber(accountNumber)
                .customerId(request.getCustomerId())
                .accountType(request.getAccountType())
                .currency(request.getCurrency())
                .balance(initialBalance)
                .availableBalance(initialBalance)
                .blockedAmount(BigDecimal.ZERO)
                .pendingDebit(BigDecimal.ZERO)
                .pendingCredit(BigDecimal.ZERO)
                .status("ACTIVE")
                .dailyTransactionLimit(request.getDailyLimit() != null ?
                        request.getDailyLimit() : new BigDecimal("10000"))
                .monthlyTransactionLimit(request.getMonthlyLimit() != null ?
                        request.getMonthlyLimit() : new BigDecimal("100000"))
                .minimumBalance(BigDecimal.ZERO)
                .overdraftLimit(BigDecimal.ZERO)
                .interestRate(BigDecimal.ZERO)
                .accountPurpose("PERSONAL")
                .openedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return accountRepository.save(account)
                .doOnSuccess(saved -> {
                    log.info("✅ Account created: {} for customer: {}",
                            saved.getAccountNumber(), saved.getCustomerId());
                    publishAccountCreatedEvent(saved);
                })
                .map(this::toDto);
    }

    @Override
    public Mono<AccountDto> getAccount(String accountId) {
        return accountRepository.findByAccountId(accountId)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found")));
    }

    @Override
    public Mono<AccountDto> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found")));
    }

    @Override
    public Flux<AccountDto> getCustomerAccounts(String customerId) {
        return accountRepository.findByCustomerId(customerId)
                .map(this::toDto);
    }

    @Override
    public Mono<AccountDto> getPrimaryAccount(String customerId) {
        return accountRepository.findPrimaryAccountByCustomer(customerId)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new RuntimeException("No primary account found for customer")));
    }

    @Override
    @Transactional
    public Mono<AccountDto> debitAccount(DebitAccountRequest request) {
        log.info("💸 Debiting account: {} amount: {}", request.getAccountId(), request.getAmount());

        return accountRepository.findByAccountId(request.getAccountId())
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found")))
                .flatMap(account -> {
                    // Validate account is active
                    if (!"ACTIVE".equals(account.getStatus())) {
                        return Mono.error(new RuntimeException("Account is not active"));
                    }

                    // Check sufficient balance
                    if (account.getAvailableBalance().compareTo(request.getAmount()) < 0) {
                        return Mono.error(new RuntimeException("Insufficient balance"));
                    }

                    // Perform debit
                    account.setBalance(account.getBalance().subtract(request.getAmount()));
                    account.setAvailableBalance(account.getAvailableBalance().subtract(request.getAmount()));
                    account.setUpdatedAt(LocalDateTime.now());

                    return accountRepository.save(account);
                })
                .doOnSuccess(account -> {
                    log.info("✅ Account debited: {} new balance: {}",
                            account.getAccountNumber(), account.getBalance());
                    publishBalanceChangedEvent(account, "DEBIT", request.getAmount());
                })
                .map(this::toDto);
    }

    @Override
    @Transactional
    public Mono<AccountDto> creditAccount(CreditAccountRequest request) {
        log.info("💰 Crediting account: {} amount: {}", request.getAccountId(), request.getAmount());

        return accountRepository.findByAccountId(request.getAccountId())
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found")))
                .flatMap(account -> {
                    // Perform credit
                    account.setBalance(account.getBalance().add(request.getAmount()));
                    account.setAvailableBalance(account.getAvailableBalance().add(request.getAmount()));
                    account.setUpdatedAt(LocalDateTime.now());

                    return accountRepository.save(account);
                })
                .doOnSuccess(account -> {
                    log.info("✅ Account credited: {} new balance: {}",
                            account.getAccountNumber(), account.getBalance());
                    publishBalanceChangedEvent(account, "CREDIT", request.getAmount());
                })
                .map(this::toDto);
    }

    @Override
    public Mono<BigDecimal> getBalance(String accountId) {
        return accountRepository.findByAccountId(accountId)
                .map(Account::getBalance)
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found")));
    }

    @Override
    public Mono<BigDecimal> getAvailableBalance(String accountId) {
        return accountRepository.findByAccountId(accountId)
                .map(Account::getAvailableBalance)
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found")));
    }

    @Override
    @Transactional
    public Mono<Void> holdFunds(String accountId, BigDecimal amount, String reference) {
        log.info("🔒 Holding funds on account: {} amount: {}", accountId, amount);

        return accountRepository.findByAccountId(accountId)
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found")))
                .flatMap(account -> {
                    if (account.getAvailableBalance().compareTo(amount) < 0) {
                        return Mono.error(new RuntimeException("Insufficient available balance"));
                    }

                    account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
                    account.setBlockedAmount(account.getBlockedAmount().add(amount));
                    account.setUpdatedAt(LocalDateTime.now());

                    return accountRepository.save(account);
                })
                .then()
                .doOnSuccess(v -> log.info("✅ Funds held on account: {}", accountId));
    }

    @Override
    @Transactional
    public Mono<Void> releaseFunds(String accountId, BigDecimal amount, String reference) {
        log.info("🔓 Releasing held funds on account: {} amount: {}", accountId, amount);

        return accountRepository.findByAccountId(accountId)
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found")))
                .flatMap(account -> {
                    account.setAvailableBalance(account.getAvailableBalance().add(amount));
                    account.setBlockedAmount(account.getBlockedAmount().subtract(amount));
                    account.setUpdatedAt(LocalDateTime.now());

                    return accountRepository.save(account);
                })
                .then()
                .doOnSuccess(v -> log.info("✅ Funds released on account: {}", accountId));
    }

    @Override
    public Mono<Void> freezeAccount(String accountId, String reason) {
        return accountRepository.findByAccountId(accountId)
                .flatMap(account -> {
                    account.setStatus("FROZEN");
                    account.setUpdatedAt(LocalDateTime.now());
                    return accountRepository.save(account);
                })
                .then()
                .doOnSuccess(v -> log.info("❄️ Account frozen: {}", accountId));
    }

    @Override
    public Mono<Void> unfreezeAccount(String accountId) {
        return accountRepository.findByAccountId(accountId)
                .flatMap(account -> {
                    account.setStatus("ACTIVE");
                    account.setUpdatedAt(LocalDateTime.now());
                    return accountRepository.save(account);
                })
                .then()
                .doOnSuccess(v -> log.info("✅ Account unfrozen: {}", accountId));
    }

    @Override
    public Mono<Void> closeAccount(String accountId) {
        return accountRepository.findByAccountId(accountId)
                .flatMap(account -> {
                    if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
                        return Mono.error(new RuntimeException("Cannot close account with non-zero balance"));
                    }

                    account.setStatus("CLOSED");
                    account.setClosedAt(LocalDateTime.now());
                    account.setUpdatedAt(LocalDateTime.now());
                    return accountRepository.save(account);
                })
                .then()
                .doOnSuccess(v -> log.info("🔒 Account closed: {}", accountId));
    }

    @Override
    public Mono<Boolean> hasSufficientBalance(String accountId, BigDecimal amount) {
        return accountRepository.findByAccountId(accountId)
                .map(account -> account.getAvailableBalance().compareTo(amount) >= 0)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Boolean> isWithinDailyLimit(String accountId, BigDecimal amount) {
        return accountRepository.findByAccountId(accountId)
                .map(account -> account.getDailyTransactionLimit().compareTo(amount) >= 0)
                .defaultIfEmpty(false);
    }

    // Helper methods
    private String generateAccountNumber() {
        return "ACC-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void publishAccountCreatedEvent(Account account) {
        // Kafka event publishing logic
        try {
            kafkaTemplate.send("account-created", account.getAccountId(), toDto(account));
            log.info("📤 Published account created event: {}", account.getAccountId());
        } catch (Exception e) {
            log.error("❌ Failed to publish account created event", e);
        }
    }

    private void publishBalanceChangedEvent(Account account, String type, BigDecimal amount) {
        // Kafka event publishing logic
        try {
            kafkaTemplate.send("balance-changed", account.getAccountId(),
                    Map.of("accountId", account.getAccountId(),
                            "type", type,
                            "amount", amount,
                            "newBalance", account.getBalance()));
            log.info("📤 Published balance changed event: {}", account.getAccountId());
        } catch (Exception e) {
            log.error("❌ Failed to publish balance changed event", e);
        }
    }

    private AccountDto toDto(Account account) {
        return AccountDto.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomerId())
                .accountType(account.getAccountType())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .blockedAmount(account.getBlockedAmount())
                .status(account.getStatus())
                .dailyTransactionLimit(account.getDailyTransactionLimit())
                .build();
    }

    @Override
    public Mono<AccountStatementDto> getAccountStatement(
            String accountId, LocalDateTime startDate, LocalDateTime endDate) {

        log.info("📄 Generating statement for account: {}", accountId);

        String formattedStart = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String formattedEnd = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return accountRepository.findByAccountId(accountId)
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found")))
                .flatMap(account ->

                        // Calls Transaction Service via the client wrapper
                        transactionServiceClient
                                .getTransactionsByAccount(accountId, formattedStart, formattedEnd)
                                .collectList()
                                .map(transactions -> buildStatement(account, transactions, startDate, endDate))
                );
    }



    private AccountStatementDto buildStatement(
            Account account,
            List<StatementTransactionDto> transactions,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        BigDecimal totalDebits = transactions.stream()
                .filter(t -> "DEBIT".equals(t.getTransactionCategory()))
                .map(StatementTransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = transactions.stream()
                .filter(t -> "CREDIT".equals(t.getTransactionCategory()))
                .map(StatementTransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Opening balance = current balance + total debits - total credits
        BigDecimal openingBalance = account.getBalance()
                .add(totalDebits)
                .subtract(totalCredits);

        List<StatementTransactionDto> withRunningBalance =
                calculateRunningBalance(transactions, openingBalance);

        return AccountStatementDto.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomerId())
                .statementStartDate(startDate)
                .statementEndDate(endDate)
                .openingBalance(openingBalance)
                .closingBalance(account.getBalance())
                .totalDebits(totalDebits)
                .totalCredits(totalCredits)
                .transactions(withRunningBalance)
                .build();
    }


    // Helper: calculates running balance per transaction
    private List<StatementTransactionDto> calculateRunningBalance(
            List<StatementTransactionDto> transactions, BigDecimal openingBalance) {

        BigDecimal[] runningBalance = { openingBalance }; // array trick for lambda mutation

        return transactions.stream()
                .sorted((a, b) -> a.getTransactionDate().compareTo(b.getTransactionDate()))
                .map(transaction -> {
                    if ("DEBIT".equals(transaction.getTransactionCategory())) {
                        runningBalance[0] = runningBalance[0].subtract(transaction.getAmount());
                    } else {
                        runningBalance[0] = runningBalance[0].add(transaction.getAmount());
                    }

                    // Return new instance with running balance filled in
                    return StatementTransactionDto.builder()
                            .transactionId(transaction.getTransactionId())
                            .referenceNumber(transaction.getReferenceNumber())
                            .transactionType(transaction.getTransactionType())
                            .transactionCategory(transaction.getTransactionCategory())
                            .amount(transaction.getAmount())
                            .currency(transaction.getCurrency())
                            .runningBalance(runningBalance[0])
                            .description(transaction.getDescription())
                            .transactionDate(transaction.getTransactionDate())
                            .build();
                })
                .collect(Collectors.toList());
    }
}

