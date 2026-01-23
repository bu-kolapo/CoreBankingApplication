package com.payments.service.service;

import com.commonlib.response.PaymentResponseDto;
import com.payments.service.dto.request.PaymentDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentService {
    /**
     * Initiate a new payment with idempotency support
     * @param request Payment request details
     * @return Payment response with payment ID and status
     */
    Mono<PaymentResponseDto> initiatePayment(PaymentDto request);

    /**
     * Process a new payment (internal, called after idempotency check)
     * @param request Payment request details
     * @return Payment response
     */
    Mono<PaymentResponseDto> processNewPayment(PaymentDto request);

    /**
     * Get payment details by payment ID
     * @param paymentId Unique payment identifier
     * @return Payment details
     */
    Mono<PaymentDto> getPaymentByPaymentId(String paymentId);

    /**
     * Get all payments for a specific order
     * @param orderId Order identifier
     * @return Stream of payments
     */
    Flux<PaymentDto> getPaymentsByOrderId(Long orderId);

    /**
     * Update payment status (usually called by webhook service)
     * @param paymentId Payment identifier
     * @param status New status
     * @param failureReason Reason if failed
     * @return Updated payment
     */
    Mono<PaymentDto> updatePaymentStatus(String paymentId, String status, String failureReason);

}

