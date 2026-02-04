package com.orders.service.events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent {
    private String orderId;
    private String orderNumber;
    private String customerId;
    private String orderType;
    private String amount;
    private String currency;
    private String status;
}