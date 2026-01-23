package com.orders.service.repository;

import com.orders.service.model.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {
    Mono<Order> findByOrderId(String orderId);
    Flux<Order> findByCustomerId(Long customerId);
    Flux<Order> findByStatus(String status);

    @Query("SELECT * FROM orders WHERE customer_id = :customerId " +
            "ORDER BY order_date DESC LIMIT :limit")
    Flux<Order> findRecentOrdersByCustomerId(Long customerId, int limit);
}
