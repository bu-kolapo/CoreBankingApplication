package com.account.service.clients;


import com.account.service.dto.StatementTransactionDto;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class TransactionServiceClient {

    private final WebClient webClient;

    // Spring injects WebClient.Builder automatically - no config class needed
    public TransactionServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.transaction.base-url}") String transactionServiceUrl) {

        this.webClient = webClientBuilder
                .baseUrl(transactionServiceUrl)
                .build();
    }

    /**
     * Calls Transaction Service to fetch transactions for an account within a date range
     */
    public Flux<StatementTransactionDto> getTransactionsByAccount(
            String accountId, String startDate, String endDate) {

        log.info("📞 Calling Transaction Service for account: {}", accountId);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/transactions/account/{accountId}")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .build(accountId))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> {
                            log.error("❌ Transaction Service client error: {}", response.statusCode());
                            return response.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Transaction Service error: " + body));
                        }
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> {
                            log.error("❌ Transaction Service server error: {}", response.statusCode());
                            return response.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Transaction Service unavailable: " + body));
                        }
                )
                .bodyToFlux(StatementTransactionDto.class)
                .doOnComplete(() -> log.info("✅ Successfully fetched transactions for account: {}", accountId))
                .doOnError(error -> log.error("❌ Failed to fetch transactions for account: {}", accountId, error));
    }
}