package com.orders.service.model;

// orders-domain/src/main/java/com.bank.orders/model/Order.java
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Table("orders")
    public class Order {
        @Id
        private Long id;
        private String orderId; // Unique order identifier
        private Long customerId;
        private BigDecimal totalAmount;
        private String currency;
        private String status; // PENDING, PAID, CANCELLED, REFUNDED
        private String orderType;
        private String description;
        private LocalDateTime orderDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }