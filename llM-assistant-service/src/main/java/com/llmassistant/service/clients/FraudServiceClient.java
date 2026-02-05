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
public class FraudServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.fraud}")
    private String fraudServiceUrl;

    public Mono<EnrichedContext.FraudInfo> getFraudInfo(String customerId) {
        return webClientBuilder.build()
                .get()
                .uri(fraudServiceUrl + "/api/fraud/customer/" + customerId)
                .retrieve()
                .bodyToMono(EnrichedContext.FraudInfo.class)
                .timeout(Duration.ofSeconds(5))
                .doOnError(error -> log.error("Error fetching fraud info for customer: {}", customerId, error))
                .onErrorResume(error -> Mono.empty());
    }
}
