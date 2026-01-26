package com.payments.service.service;

import com.payments.service.dto.response.GatewayResponse;
import com.payments.service.model.Payment;
import com.stripe.model.PaymentIntent;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PaymentGatewayService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;


    public Mono<GatewayResponse> createPaymentSession(Payment payment) {
        return Mono.fromCallable(() -> {
                    Stripe.apiKey = stripeApiKey;

                    Map<String, Object> params = new HashMap<>();
                    params.put("amount", payment.getAmount()
                            .multiply(new BigDecimal("100"))
                            .longValue());
                    params.put("currency", payment.getCurrency().toLowerCase());
                    params.put("payment_method_types", List.of("card"));

                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("payment_id", payment.getPaymentId());
                    metadata.put("order_id", payment.getOrderId());
                    params.put("metadata", metadata);

                    PaymentIntent intent = PaymentIntent.create(params);

                    return GatewayResponse.builder()
                            .transactionId(intent.getId().toString())
                            .status(intent.getStatus())
                            .clientSecret(intent.getClientSecret())
                            .build();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(StripeException.class, ex ->
                        new RuntimeException("Stripe payment creation failed", ex)
                );
    }
}
