package com.orders.service.service;

import com.orders.service.dto.OrderDto;
import com.orders.service.dto.request.OrderRequest;
import com.orders.service.dto.request.OrderStatusRequest;
import com.orders.service.events.PaymentCompletedEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {


    // Create a new order and initiate payment
    Mono<OrderDto> createOrder(OrderRequest request);

    // Get order details
    Mono<OrderDto> getOrder(String orderId);

    Mono<OrderDto> getOrderByNumber(String orderNumber);

    // Get customer's orders
    Flux<OrderDto> getCustomerOrders(String customerId);

    Flux<OrderDto> getCustomerOrdersByStatus(String customerId, String status);

    // Update order status (admin/system)
    Mono<OrderDto> updateOrderStatus(String orderId, OrderStatusRequest request);

    // Cancel order
    Mono<OrderDto> cancelOrder(String orderId, String reason);

    // Handle payment completion (from Kafka)
    Mono<OrderDto> handlePaymentCompleted(PaymentCompletedEvent event);

    // Fulfillment
    Mono<OrderDto> updateFulfillmentStatus(String orderId, String status, String trackingNumber);
}
