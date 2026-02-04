package com.orders.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private String orderId;
    private String orderNumber;
    private String customerId;
    private String accountId;
    private String orderType;
    private String orderStatus;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String paymentId;
    private String transactionId;
    private String paymentStatus;
    private String fulfillmentStatus;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}