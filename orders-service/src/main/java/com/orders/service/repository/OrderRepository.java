package com.orders.service.repository;

import com.orders.service.model.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    Mono<Order> findByOrderId(String orderId);

    Mono<Order> findByOrderNumber(String orderNumber);

    Mono<Order> findByPaymentId(String paymentId);

    Flux<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    Flux<Order> findByCustomerIdAndOrderStatusOrderByCreatedAtDesc(String customerId, String status);

    Flux<Order> findByOrderStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT * FROM orders WHERE customer_id = :customerId AND created_at BETWEEN :start AND :end ORDER BY created_at DESC")
    Flux<Order> findByCustomerAndDateRange(String customerId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(*) FROM orders WHERE customer_id = :customerId AND order_status = :status")
    Mono<Long> countByCustomerAndStatus(String customerId, String status);
}
