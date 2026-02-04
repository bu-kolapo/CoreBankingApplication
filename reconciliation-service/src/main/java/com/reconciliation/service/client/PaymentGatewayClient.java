package com.reconciliation.service.client;

import com.reconciliation.service.dto.GatewaySettlementRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class PaymentGatewayClient {

    private final WebClient webClient;

    @Value("${services.stripe.api-key}")
    private String stripeApiKey;

    public PaymentGatewayClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.stripe.base-url}") String stripeBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(stripeBaseUrl).build();
    }

    /**
     * Fetches settlement/balance transactions from Stripe for a specific date
     */
    public Flux<GatewaySettlementRecord> getStripeSettlements(String date) {
        log.info("📞 Fetching Stripe settlements for date: {}", date);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/balance/transactions")
                        .queryParam("created[gte]", date + "T00:00:00")
                        .queryParam("created[lte]", date + "T23:59:59")
                        .queryParam("limit", "100")
                        .build())
                .header("Authorization", "Bearer " + stripeApiKey)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Stripe 4xx: " + body))
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Stripe 5xx: " + body))
                )
                .bodyToFlux(GatewaySettlementRecord.class)
                .doOnComplete(() -> log.info("✅ Fetched Stripe settlements for: {}", date))
                .doOnError(e -> log.error("❌ Failed to fetch Stripe settlements", e));
    }
}
