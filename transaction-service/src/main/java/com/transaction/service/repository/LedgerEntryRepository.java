package com.transaction.service.repository;

import com.transaction.service.model.LedgerEntry;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface LedgerEntryRepository extends ReactiveCrudRepository<LedgerEntry, Long> {

    Mono<LedgerEntry> findByEntryId(String entryId);

    Flux<LedgerEntry> findByTransactionId(String transactionId);

    Flux<LedgerEntry> findByAccountId(String accountId);

    Flux<LedgerEntry> findByAccountIdAndEntryDateBetween(
            String accountId, LocalDateTime start, LocalDateTime end);

    Flux<LedgerEntry> findByAccountIdOrderByEntryDateDesc(String accountId);
}