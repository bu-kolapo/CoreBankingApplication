package com.llmassistant.service.service;


import com.llmassistant.service.clients.AccountServiceClient;
import com.llmassistant.service.clients.FraudServiceClient;
import com.llmassistant.service.clients.TransactionServiceClient;
import com.llmassistant.service.model.EnrichedContext;
import com.llmassistant.service.model.LlmRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContextEnrichmentService {

    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;
    private final FraudServiceClient fraudServiceClient;

    public Mono<EnrichedContext> enrichContext(LlmRequest request) {
        if (request.getCustomerId() == null) {
            return Mono.just(EnrichedContext.builder().build());
        }

        Mono<EnrichedContext.AccountInfo> accountMono = accountServiceClient
                .getAccountInfo(request.getCustomerId())
                .defaultIfEmpty(EnrichedContext.AccountInfo.builder().build());

        Mono<EnrichedContext.FraudInfo> fraudMono = fraudServiceClient
                .getFraudInfo(request.getCustomerId())
                .defaultIfEmpty(EnrichedContext.FraudInfo.builder().build());

        return accountMono.flatMap(accountInfo -> {
            if (accountInfo.getAccountId() != null) {
                return transactionServiceClient.getRecentTransactions(accountInfo.getAccountId(), 5)
                        .collectList()
                        .zipWith(fraudMono)
                        .map(tuple -> EnrichedContext.builder()
                                .accountInfo(accountInfo)
                                .recentTransactions(tuple.getT1())
                                .fraudInfo(tuple.getT2())
                                .build());
            } else {
                return fraudMono.map(fraudInfo -> EnrichedContext.builder()
                        .accountInfo(accountInfo)
                        .fraudInfo(fraudInfo)
                        .build());
            }
        });
    }
}
