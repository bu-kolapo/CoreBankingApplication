package com.orders.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;


        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Table("orders")
        public class Order {

            @Id
            private Long id;

            private String orderId;                 // UUID - our order ID
            private String orderNumber;             // Customer-facing: ORD-000001
            private String customerId;              // Who placed this order
            private String accountId;               // Which account is paying

            // Order details
            private String orderType;               // PURCHASE, TRANSFER, SUBSCRIPTION, TOP_UP
            private String orderStatus;             // PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED
            private BigDecimal amount;
            private String currency;
            private String description;

            // Payment tracking
            private String paymentId;               // Link to Payment Service
            private String transactionId;           // Link to Transaction Service
            private String paymentStatus;           // Mirrors payment status for quick lookup

            // Fulfillment
            private String fulfillmentStatus;       // PENDING, PROCESSING, SHIPPED, DELIVERED, FAILED
            private String trackingNumber;
            private LocalDateTime estimatedDelivery;

            // Items (stored as JSON for simplicity, or use separate OrderItem table)
            private String items;                   // JSON array of items

            // Metadata
            private String shippingAddress;         // JSON object
            private String billingAddress;          // JSON object
            private String metadata;                // Additional custom fields

            // Cancellation/Refund
            private String cancellationReason;
            private String refundId;
            private LocalDateTime cancelledAt;
            private LocalDateTime refundedAt;

            // Audit
            private LocalDateTime createdAt;
            private LocalDateTime updatedAt;
            private LocalDateTime completedAt;
        }
