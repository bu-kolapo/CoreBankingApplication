package com.payments.service.controller;

import com.commonlib.response.ApiResponse;
import com.commonlib.response.PaymentResponseDto;
import com.payments.service.dto.request.PaymentDto;
import com.payments.service.service.Implementations.PaymentServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentServiceImpl paymentService;

    public PaymentController(PaymentServiceImpl paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiResponse<PaymentResponseDto>> initiatePayment(
            @Valid @RequestBody PaymentDto request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        log.info("📥 Received payment request for order: {}", request.getOrderId());

        if (idempotencyKey != null) {
            request.setIdempotencyKey(idempotencyKey);
        }

        return paymentService.initiatePayment(request)
                .map(ApiResponse::success)
                .doOnSuccess(response -> log.info("✅ Payment initiated: {}",
                        response.getData().getPaymentId()))
                .onErrorResume(error -> {
                    log.error("❌ Payment initiation failed", error);
                    return Mono.just(ApiResponse.error(error.getMessage(), "PAYMENT_ERROR"));
                });
    }

    @GetMapping("/{paymentId}")
    public Mono<ApiResponse<PaymentDto>> getPayment(@PathVariable String paymentId) {
        log.info("📥 Fetching payment: {}", paymentId);

        return paymentService.getPaymentByPaymentId(paymentId)
                .map(ApiResponse::success)
                .onErrorResume(error ->
                        Mono.just(ApiResponse.error("Payment not found", "NOT_FOUND")));
    }

    @GetMapping("/order/{orderId}")
    public Flux<PaymentDto> getPaymentsByOrder(@PathVariable Long orderId) {
        log.info("📥 Fetching payments for order: {}", orderId);
        return paymentService.getPaymentsByOrderId(orderId);
    }

    @PutMapping("/{paymentId}/status")
    public Mono<ApiResponse<PaymentDto>> updatePaymentStatus(
            @PathVariable String paymentId,
            @RequestParam String status,
            @RequestParam(required = false) String failureReason) {

        log.info("📥 Updating payment {} status to: {}", paymentId, status);

        return paymentService.updatePaymentStatus(paymentId, status, failureReason)
                .map(ApiResponse::success)
                .onErrorResume(error ->
                        Mono.just(ApiResponse.error(error.getMessage(), "UPDATE_ERROR")));
    }

    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("Payment Service is UP");
    }
}