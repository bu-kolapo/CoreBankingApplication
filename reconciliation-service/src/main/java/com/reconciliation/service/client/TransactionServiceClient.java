package com.reconciliation.service.client;

import com.reconciliation.service.dto.InternalTransactionRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class TransactionServiceClient {

    private final WebClient webClient;

    public TransactionServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.transaction.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Fetches all transactions for a specific date from Transaction Service
     */
    public Flux<InternalTransactionRecord> getTransactionsByDate(String date) {
        log.info("📞 Fetching internal transactions for date: {}", date);

        return webClient.get()
                .uri("/api/transactions/date/{date}", date)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Transaction Service 4xx: " + body))
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Transaction Service 5xx: " + body))
                )
                .bodyToFlux(InternalTransactionRecord.class)
                .doOnComplete(() -> log.info("✅ Fetched internal transactions for: {}", date))
                .doOnError(e -> log.error("❌ Failed to fetch internal transactions", e));
    }
}