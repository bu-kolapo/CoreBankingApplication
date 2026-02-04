package com.orders.service.repository;

import com.orders.service.model.OrderItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {

    Mono<OrderItem> findByItemId(String itemId);

    Flux<OrderItem> findByOrderId(String orderId);
}