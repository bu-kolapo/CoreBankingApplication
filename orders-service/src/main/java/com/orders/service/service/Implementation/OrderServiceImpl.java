package com.orders.service.service.Implementation;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.orders.service.clients.PaymentServiceClient;
import com.orders.service.dto.OrderDto;
import com.orders.service.dto.OrderItemDto;
import com.orders.service.dto.request.InitiatePaymentRequest;
import com.orders.service.dto.request.OrderRequest;
import com.orders.service.dto.request.OrderStatusRequest;
import com.orders.service.events.OrderCompletedEvent;
import com.orders.service.events.PaymentCompletedEvent;
import com.orders.service.model.Order;
import com.orders.service.model.OrderItem;
import com.orders.service.repository.OrderItemRepository;
import com.orders.service.repository.OrderRepository;
import com.orders.service.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            PaymentServiceClient paymentServiceClient,
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentServiceClient = paymentServiceClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // ─── CREATE ORDER ────────────────────────────────────────────────

    @Override
    @Transactional
    public Mono<OrderDto> createOrder(OrderRequest request) {
        log.info("📦 Creating order for customer: {}", request.getCustomerId());

        String orderId = UUID.randomUUID().toString();
        String orderNumber = generateOrderNumber();

        // Serialize items, addresses to JSON
        String itemsJson = serializeToJson(request.getItems());
        String shippingJson = serializeToJson(request.getShippingAddress());
        String billingJson = serializeToJson(request.getBillingAddress());

        Order order = Order.builder()
                .orderId(orderId)
                .orderNumber(orderNumber)
                .customerId(request.getCustomerId())
                .accountId(request.getAccountId())
                .orderType(request.getOrderType())
                .orderStatus("PENDING")
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .fulfillmentStatus("PENDING")
                .items(itemsJson)
                .shippingAddress(shippingJson)
                .billingAddress(billingJson)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    // Save order items if provided
                    if (request.getItems() != null && !request.getItems().isEmpty()) {
                        return saveOrderItems(savedOrder.getOrderId(), request.getItems())
                                .then(Mono.just(savedOrder));
                    }
                    return Mono.just(savedOrder);
                })
                .flatMap(savedOrder -> initiatePaymentForOrder(savedOrder, request.getIdempotencyKey()))
                .doOnSuccess(dto -> log.info("✅ Order created: {}", dto.getOrderId()));
    }

    private Mono<OrderDto> initiatePaymentForOrder(Order order, String idempotencyKey) {
        log.info("💳 Initiating payment for order: {}", order.getOrderId());

        InitiatePaymentRequest paymentRequest = InitiatePaymentRequest.builder()
                .orderId(order.getOrderId())
                .accountId(order.getAccountId())
                .amount(order.getAmount())
                .currency(order.getCurrency())
                .paymentMethod("CARD")
                .idempotencyKey(idempotencyKey)
                .build();

        return paymentServiceClient.initiatePayment(paymentRequest)
                .flatMap(paymentResponse -> {
                    // Update order with payment ID
                    order.setPaymentId(paymentResponse.getPaymentId());
                    order.setPaymentStatus(paymentResponse.getStatus());
                    order.setOrderStatus("PROCESSING");
                    order.setUpdatedAt(LocalDateTime.now());

                    return orderRepository.save(order);
                })
                .map(this::toDto)
                .onErrorResume(error -> {
                    log.error("❌ Payment initiation failed for order: {}", order.getOrderId(), error);
                    // Mark order as failed
                    order.setOrderStatus("FAILED");
                    order.setUpdatedAt(LocalDateTime.now());
                    return orderRepository.save(order).map(this::toDto);
                });
    }

    private Flux<OrderItem> saveOrderItems(String orderId, List<OrderItemDto> items) {
        return Flux.fromIterable(items)
                .map(itemDto -> OrderItem.builder()
                        .itemId(UUID.randomUUID().toString())
                        .orderId(orderId)
                        .productId(itemDto.getProductId())
                        .productName(itemDto.getProductName())
                        .productSku(itemDto.getProductSku())
                        .quantity(itemDto.getQuantity())
                        .unitPrice(itemDto.getUnitPrice())
                        .totalPrice(itemDto.getTotalPrice())
                        .currency(itemDto.getCurrency())
                        .createdAt(LocalDateTime.now())
                        .build())
                .flatMap(orderItemRepository::save);
    }

    // ─── GET ORDERS ──────────────────────────────────────────────────

    @Override
    public Mono<OrderDto> getOrder(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .flatMap(this::enrichOrderWithItems)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found: " + orderId)));
    }

    @Override
    public Mono<OrderDto> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .flatMap(this::enrichOrderWithItems)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found: " + orderNumber)));
    }

    @Override
    public Flux<OrderDto> getCustomerOrders(String customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .flatMap(this::enrichOrderWithItems);
    }

    @Override
    public Flux<OrderDto> getCustomerOrdersByStatus(String customerId, String status) {
        return orderRepository.findByCustomerIdAndOrderStatusOrderByCreatedAtDesc(customerId, status)
                .flatMap(this::enrichOrderWithItems);
    }

    private Mono<OrderDto> enrichOrderWithItems(Order order) {
        return orderItemRepository.findByOrderId(order.getOrderId())
                .map(this::toItemDto)
                .collectList()
                .map(items -> toDto(order, items));
    }

    // ─── UPDATE ORDER ────────────────────────────────────────────────

    @Override
    @Transactional
    public Mono<OrderDto> updateOrderStatus(String orderId, OrderStatusRequest request) {
        log.info("🔄 Updating order status: {} to {}", orderId, request.getOrderStatus());

        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
                .flatMap(order -> {
                    order.setOrderStatus(request.getOrderStatus());
                    if (request.getFulfillmentStatus() != null) {
                        order.setFulfillmentStatus(request.getFulfillmentStatus());
                    }
                    if (request.getTrackingNumber() != null) {
                        order.setTrackingNumber(request.getTrackingNumber());
                    }
                    order.setUpdatedAt(LocalDateTime.now());

                    if ("COMPLETED".equals(request.getOrderStatus())) {
                        order.setCompletedAt(LocalDateTime.now());
                    }

                    return orderRepository.save(order);
                })
                .flatMap(this::enrichOrderWithItems)
                .doOnSuccess(dto -> log.info("✅ Order status updated: {}", orderId));
    }

    @Override
    @Transactional
    public Mono<OrderDto> cancelOrder(String orderId, String reason) {
        log.info("❌ Cancelling order: {} reason: {}", orderId, reason);

        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
                .flatMap(order -> {
                    if ("COMPLETED".equals(order.getOrderStatus())) {
                        return Mono.error(new RuntimeException("Cannot cancel completed order"));
                    }

                    order.setOrderStatus("CANCELLED");
                    order.setCancellationReason(reason);
                    order.setCancelledAt(LocalDateTime.now());
                    order.setUpdatedAt(LocalDateTime.now());

                    return orderRepository.save(order);
                })
                .flatMap(this::enrichOrderWithItems)
                .doOnSuccess(dto -> {
                    log.info("✅ Order cancelled: {}", orderId);
                    publishOrderCancelledEvent(dto);
                });
    }

    // ─── PAYMENT EVENT HANDLER ───────────────────────────────────────

    @Override
    @Transactional
    public Mono<OrderDto> handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("🎧 Handling payment completion for order: {}", event.getOrderId());

        return orderRepository.findByOrderId(event.getOrderId())
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found for payment event")))
                .flatMap(order -> {
                    order.setPaymentStatus(event.getStatus());
                    order.setTransactionId(event.getGatewayTransactionId());
                    order.setUpdatedAt(LocalDateTime.now());

                    if ("SUCCESS".equals(event.getStatus())) {
                        order.setOrderStatus("COMPLETED");
                        order.setCompletedAt(LocalDateTime.now());
                        order.setFulfillmentStatus("PROCESSING");
                    } else if ("FAILED".equals(event.getStatus())) {
                        order.setOrderStatus("FAILED");
                    }

                    return orderRepository.save(order);
                })
                .flatMap(this::enrichOrderWithItems)
                .doOnSuccess(dto -> {
                    log.info("✅ Order updated from payment event: {}", dto.getOrderId());
                    if ("COMPLETED".equals(dto.getOrderStatus())) {
                        publishOrderCompletedEvent(dto);
                    }
                });
    }

    // ─── FULFILLMENT ─────────────────────────────────────────────────

    @Override
    @Transactional
    public Mono<OrderDto> updateFulfillmentStatus(String orderId, String status, String trackingNumber) {
        log.info("📦 Updating fulfillment status for order: {} to {}", orderId, status);

        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
                .flatMap(order -> {
                    order.setFulfillmentStatus(status);
                    if (trackingNumber != null) {
                        order.setTrackingNumber(trackingNumber);
                    }
                    order.setUpdatedAt(LocalDateTime.now());

                    return orderRepository.save(order);
                })
                .flatMap(this::enrichOrderWithItems)
                .doOnSuccess(dto -> log.info("✅ Fulfillment status updated: {}", orderId));
    }

    // ─── KAFKA EVENTS ────────────────────────────────────────────────

    private void publishOrderCompletedEvent(OrderDto order) {
        OrderCompletedEvent event = OrderCompletedEvent.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .orderType(order.getOrderType())
                .amount(order.getAmount().toPlainString())
                .currency(order.getCurrency())
                .status(order.getOrderStatus())
                .build();

        try {
            kafkaTemplate.send("order-completed", order.getOrderId(), event);
            log.info("📤 Published order completed event: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("❌ Failed to publish order completed event", e);
        }
    }

    private void publishOrderCancelledEvent(OrderDto order) {
        try {
            kafkaTemplate.send("order-cancelled", order.getOrderId(), order);
            log.info("📤 Published order cancelled event: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("❌ Failed to publish order cancelled event", e);
        }
    }

    // ─── HELPERS ─────────────────────────────────────────────────────

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    private String serializeToJson(Object obj) {
        try {
            return obj != null ? objectMapper.writeValueAsString(obj) : null;
        } catch (Exception e) {
            log.error("Failed to serialize to JSON", e);
            return null;
        }
    }

    // ─── MAPPERS ─────────────────────────────────────────────────────

    private OrderDto toDto(Order order) {
        return toDto(order, List.of());
    }

    private OrderDto toDto(Order order, List<OrderItemDto> items) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .accountId(order.getAccountId())
                .orderType(order.getOrderType())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .currency(order.getCurrency())
                .description(order.getDescription())
                .paymentId(order.getPaymentId())
                .transactionId(order.getTransactionId())
                .paymentStatus(order.getPaymentStatus())
                .fulfillmentStatus(order.getFulfillmentStatus())
                .items(items)
                .createdAt(order.getCreatedAt())
                .completedAt(order.getCompletedAt())
                .build();
    }

    private OrderItemDto toItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .itemId(item.getItemId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .currency(item.getCurrency())
                .build();
    }
}