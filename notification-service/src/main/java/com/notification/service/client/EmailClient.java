package com.notification.service.client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class EmailClient {

    private final WebClient webClient;

    @Value("${notification.email.api-key}")
    private String apiKey;

    public EmailClient(WebClient.Builder builder,
                       @Value("${notification.email.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    /**
     * Sends an email via your chosen provider (e.g., SendGrid, Mailgun, AWS SES).
     * Returns the provider's message ID on success.
     */
    public Mono<String> send(String to, String subject, String body) {
        log.info("📧 Sending email to: {}", to);

        return webClient.post()
                .uri("/v3/mail/send")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "to", to,
                        "subject", subject,
                        "body", body
                ))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .map(b -> new RuntimeException("Email provider 4xx: " + b))
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .map(b -> new RuntimeException("Email provider 5xx: " + b))
                )
                .bodyToMono(Map.class)
                .map(response -> (String) response.getOrDefault("message_id", "unknown"))
                .doOnSuccess(id -> log.info("✅ Email sent, provider ID: {}", id))
                .doOnError(e -> log.error("❌ Email send failed to: {}", to, e));
    }
}