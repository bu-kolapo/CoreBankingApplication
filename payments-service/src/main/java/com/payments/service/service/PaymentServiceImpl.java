package com.payments.service.service;

import com.commonlib.response.PaymentResponseDto;
import com.payments.service.dto.request.PaymentDto;
import com.payments.service.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Override
    public Mono<PaymentResponseDto> initiatePayment(PaymentDto request) {
        return null;
    }

    @Override
    public Mono<PaymentResponseDto> processNewPayment(PaymentDto request) {
        return null;
    }

    @Override
    public Mono<PaymentDto> getPaymentByPaymentId(String paymentId) {
        return null;
    }

    @Override
    public Flux<PaymentDto> getPaymentsByOrderId(Long orderId) {
        return null;
    }

    @Override
    public Mono<PaymentDto> updatePaymentStatus(String paymentId, String status, String failureReason) {
        return null;
    }
}