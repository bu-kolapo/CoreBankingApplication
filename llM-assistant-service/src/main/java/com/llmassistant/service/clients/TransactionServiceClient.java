package com.llmassistant.service.clients;


import com.llmassistant.service.model.EnrichedContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.transaction}")
    private String transactionServiceUrl;

    public Flux<EnrichedContext.TransactionInfo> getRecentTransactions(String accountId, int limit) {
        return webClientBuilder.build()
                .get()
                .uri(transactionServiceUrl + "/api/transactions/account/" + accountId + "?limit=" + limit)
                .retrieve()
                .bodyToFlux(EnrichedContext.TransactionInfo.class)
                .timeout(Duration.ofSeconds(5))
                .doOnError(error -> log.error("Error fetching transactions for account: {}", accountId, error))
                .onErrorResume(error -> Flux.empty());
    }
}