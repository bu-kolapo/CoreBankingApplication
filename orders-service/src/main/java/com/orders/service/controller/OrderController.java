package com.orders.service.controller;

import com.orders.service.dto.OrderDto;
import com.orders.service.dto.request.OrderRequest;
import com.orders.service.dto.request.OrderStatusRequest;
import com.orders.service.service.OrderService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@Slf4j
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ─── CREATE ──────────────────────────────────────────────────────

    @PostMapping
    public Mono<ResponseEntity<OrderDto>> createOrder(
            @Valid @RequestBody OrderRequest request) {

        log.info("📦 Create order request from customer: {}", request.getCustomerId());

        return orderService.createOrder(request)
                .map(order -> ResponseEntity.status(HttpStatus.CREATED).body(order))
                .onErrorResume(error -> {
                    log.error("❌ Failed to create order", error);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    // ─── GET ─────────────────────────────────────────────────────────

    @GetMapping("/{orderId}")
    public Mono<ResponseEntity<OrderDto>> getOrder(@PathVariable String orderId) {
        log.info("🔍 Getting order: {}", orderId);

        return orderService.getOrder(orderId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    log.error("❌ Failed to get order", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @GetMapping("/number/{orderNumber}")
    public Mono<ResponseEntity<OrderDto>> getOrderByNumber(@PathVariable String orderNumber) {
        log.info("🔍 Getting order by number: {}", orderNumber);

        return orderService.getOrderByNumber(orderNumber)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    log.error("❌ Failed to get order", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @GetMapping("/customer/{customerId}")
    public Flux<OrderDto> getCustomerOrders(@PathVariable String customerId) {
        log.info("🔍 Getting orders for customer: {}", customerId);
        return orderService.getCustomerOrders(customerId);
    }

    @GetMapping("/customer/{customerId}/status/{status}")
    public Flux<OrderDto> getCustomerOrdersByStatus(
            @PathVariable String customerId,
            @PathVariable String status) {

        log.info("🔍 Getting {} orders for customer: {}", status, customerId);
        return orderService.getCustomerOrdersByStatus(customerId, status);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────

    @PutMapping("/{orderId}/status")
    public Mono<ResponseEntity<OrderDto>> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody OrderStatusRequest request) {

        log.info("🔄 Updating order status: {} to {}", orderId, request.getOrderStatus());

        return orderService.updateOrderStatus(orderId, request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("❌ Failed to update order status", error);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @PostMapping("/{orderId}/cancel")
    public Mono<ResponseEntity<OrderDto>> cancelOrder(
            @PathVariable String orderId,
            @RequestParam String reason) {

        log.info("❌ Cancelling order: {} reason: {}", orderId, reason);

        return orderService.cancelOrder(orderId, reason)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("❌ Failed to cancel order", error);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @PutMapping("/{orderId}/fulfillment")
    public Mono<ResponseEntity<OrderDto>> updateFulfillmentStatus(
            @PathVariable String orderId,
            @RequestParam String status,
            @RequestParam(required = false) String trackingNumber) {

        log.info("📦 Updating fulfillment for order: {} to {}", orderId, status);

        return orderService.updateFulfillmentStatus(orderId, status, trackingNumber)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("❌ Failed to update fulfillment status", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}