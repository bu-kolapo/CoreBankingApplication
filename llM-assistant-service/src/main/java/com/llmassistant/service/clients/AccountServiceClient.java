package com.llmassistant.service.clients;


import com.llmassistant.service.model.EnrichedContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.account}")
    private String accountServiceUrl;

    public Mono<EnrichedContext.AccountInfo> getAccountInfo(String customerId) {
        return webClientBuilder.build()
                .get()
                .uri(accountServiceUrl + "/api/accounts/customer/" + customerId)
                .retrieve()
                .bodyToMono(EnrichedContext.AccountInfo.class)
                .timeout(Duration.ofSeconds(5))
                .doOnError(error -> log.error("Error fetching account info for customer: {}", customerId, error))
                .onErrorResume(error -> Mono.empty());
    }
}