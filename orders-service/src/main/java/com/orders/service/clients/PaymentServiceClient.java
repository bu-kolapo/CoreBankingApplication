package com.orders.service.clients;


import com.orders.service.dto.PaymentResponseDto;
import com.orders.service.dto.request.InitiatePaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PaymentServiceClient {

    private final WebClient webClient;

    public PaymentServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.payment.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Calls Payment Service to initiate a payment for this order
     */
    public Mono<PaymentResponseDto> initiatePayment(InitiatePaymentRequest request) {
        log.info("📞 Initiating payment for order: {}", request.getOrderId());

        return webClient.post()
                .uri("/api/payments")
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Payment Service 4xx: " + body))
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Payment Service 5xx: " + body))
                )
                .bodyToMono(PaymentResponseDto.class)
                .doOnSuccess(payment -> log.info("✅ Payment initiated: {}", payment.getPaymentId()))
                .doOnError(e -> log.error("❌ Payment initiation failed", e));
    }
}