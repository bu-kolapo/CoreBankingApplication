package com.payments.service.service.Implementations;

import com.commonlib.event.PaymentEvent;
import com.commonlib.messaging.PaymentEventPublisher;
import com.commonlib.response.PaymentResponseDto;
import com.payments.service.dto.request.PaymentDto;
import com.payments.service.model.Payment;
import com.payments.service.repository.PaymentRepository;
import com.payments.service.service.IdempotencyService;
import com.payments.service.service.PaymentGatewayService;
import com.payments.service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private IdempotencyService idempotencyService;
    private PaymentRepository paymentRepository;
    private PaymentGatewayService paymentGatewayService;
    private  PaymentEventPublisher eventPublisher;

    public PaymentServiceImpl(IdempotencyService idempotencyService, PaymentRepository paymentRepository, PaymentGatewayService paymentGatewayService, PaymentEventPublisher eventPublisher) {
        this.idempotencyService = idempotencyService;
        this.paymentRepository=paymentRepository;
        this.paymentGatewayService=paymentGatewayService;
        this.eventPublisher=eventPublisher;
    }

    @Override
    @Transactional
    public Mono<PaymentResponseDto> initiatePayment(PaymentDto request) {
        log.info("🚀 Initiating payment for order: {}", request.getOrderId());

        return idempotencyService.checkIdempotency(
                        request.getIdempotencyKey(), "POST", "/api/payments")
                .flatMap(cached -> {
                    if (cached != null) {
                        log.info("♻️ Idempotency hit - returning cached response");
                        return Mono.just(cached);
                    }
                    return processNewPayment(request);
                });
    }

    @Override
    @Transactional
    public Mono<PaymentResponseDto> processNewPayment(PaymentDto request) {
        log.info("💳 Processing new payment: {}", request.getOrderId());

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .orderId(request.getOrderId())
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .paymentGateway("STRIPE")
                .status("INITIATED")
                .customerEmail(request.getCustomerEmail())
                .callbackUrl(request.getCallbackUrl())
                .webhookUrl(request.getWebhookUrl())
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment)
                .doOnSuccess(saved -> log.info("💾 Payment saved: {}", saved.getPaymentId()))
                .flatMap(savedPayment -> {
                    // Publish event
                    publishPaymentCreatedEvent(savedPayment);

                    // Call payment gateway
                    return paymentGatewayService.createPaymentSession(savedPayment)
                            .flatMap(gatewayResponse -> {
                                savedPayment.setGatewayTransactionId(gatewayResponse.getTransactionId());
                                savedPayment.setStatus("PROCESSING");
                                savedPayment.setUpdatedAt(LocalDateTime.now());

                                return paymentRepository.save(savedPayment);
                            })
                            .map(this::toPaymentResponseDto)
                            .flatMap(response -> {
                                if (request.getIdempotencyKey() != null) {
                                    return idempotencyService.storeResponse(
                                            request.getIdempotencyKey(),
                                            "POST",
                                            "/api/payments",
                                            response,
                                            200
                                    ).thenReturn(response);
                                }
                                return Mono.just(response);
                            });
                })
                .doOnError(error -> log.error("❌ Payment processing failed", error));
    }

    @Override
    public Mono<PaymentDto> getPaymentByPaymentId(String paymentId) {

        log.info("🔍 Fetching payment: {}", paymentId);

        return paymentRepository.findByPaymentId(paymentId)
                .map(this::toPaymentDto)
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Payment not found: " + paymentId)));
    }

    @Override
    public Flux<PaymentDto> getPaymentsByOrderId(Long orderId) {
        log.info("🔍 Fetching payments for order: {}", orderId);

        return paymentRepository.findByOrderId(orderId)
                .map(this::toPaymentDto);
    }

    @Override
    public Mono<PaymentDto> updatePaymentStatus(String paymentId, String status, String failureReason) {
        log.info("🔄 Updating payment {} to status: {}", paymentId, status);

        return paymentRepository.findByPaymentId(paymentId)
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Payment not found: " + paymentId)))
                .flatMap(payment -> {
                    payment.setStatus(status);
                    payment.setUpdatedAt(LocalDateTime.now());

                    if ("SUCCESS".equals(status)) {
                        payment.setPaidAt(LocalDateTime.now());
                    } else if ("FAILED".equals(status)) {
                        payment.setFailureReason(failureReason);
                    }

                    return paymentRepository.save(payment)
                            .doOnSuccess(updated -> {
                                if ("SUCCESS".equals(status)) {
                                    publishPaymentSuccessEvent(updated);
                                } else if ("FAILED".equals(status)) {
                                    publishPaymentFailedEvent(updated);
                                }
                            })
                            .map(this::toPaymentDto);
                });
    }
    private void publishPaymentCreatedEvent(Payment payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .paymentId(payment.getPaymentId())
                .eventType("PAYMENT_CREATED")
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisher.publishPaymentCreated(event);
        log.info("✅ Published payment.created event");
    }

    private void publishPaymentSuccessEvent(Payment payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .paymentId(payment.getPaymentId())
                .eventType("PAYMENT_SUCCESS")
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status("SUCCESS")
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisher.publishPaymentSuccess(event);
        eventPublisher.publishReconciliationRequest(event);
        log.info("✅ Published payment.success event");
    }

    private void publishPaymentFailedEvent(Payment payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .paymentId(payment.getPaymentId())
                .eventType("PAYMENT_FAILED")
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status("FAILED")
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisher.publishPaymentFailed(event);
        log.info("✅ Published payment.failed event");
    }

    private PaymentResponseDto toPaymentResponseDto(Payment payment) {
        return PaymentResponseDto.builder()
                .paymentId(payment.getPaymentId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private PaymentDto toPaymentDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .paymentId(payment.getPaymentId())
                .orderId((payment.getOrderId()))
                .accountId(payment.getAccountId())
                .paymentGateway(payment.getPaymentGateway())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .customerEmail(payment.getCustomerEmail())
                .retryCount(payment.getRetryCount())
                .failureReason(payment.getFailureReason())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }


}
