package com.customer.service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreatedEvent {
    private String customerId;
    private String customerNumber;
    private String customerType;
    private String email;
    private String phoneNumber;
    private String accountType;
    private String status;
    private String preferredCurrency;
}
