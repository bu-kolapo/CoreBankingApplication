package com.notification.service.client;



import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class SmsClient {

    private final WebClient webClient;

    @Value("${notification.sms.account-sid}")
    private String accountSid;

    @Value("${notification.sms.auth-token}")
    private String authToken;

    @Value("${notification.sms.from-number}")
    private String fromNumber;

    public SmsClient(WebClient.Builder builder,
                     @Value("${notification.sms.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    /**
     * Sends an SMS via Twilio (or swap to any other provider).
     * Returns the provider's message SID on success.
     */
    public Mono<String> send(String toPhone, String body) {
        log.info("📱 Sending SMS to: {}", toPhone);

        return webClient.post()
                .uri("/2010-04-01/Accounts/" + accountSid + "/Messages.json")
                .headers(headers -> headers.setBasicAuth(accountSid, authToken))
                .bodyValue(Map.of(
                        "From", fromNumber,
                        "To", toPhone,
                        "Body", body
                ))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .map(b -> new RuntimeException("SMS provider 4xx: " + b))
                )
                .bodyToMono(Map.class)
                .map(response -> (String) response.getOrDefault("sid", "unknown"))
                .doOnSuccess(sid -> log.info("✅ SMS sent, provider SID: {}", sid))
                .doOnError(e -> log.error("❌ SMS send failed to: {}", toPhone, e));
    }
}