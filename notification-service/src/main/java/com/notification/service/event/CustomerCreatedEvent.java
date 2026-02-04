package com.notification.service.event;


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
    private String email;
    private String phoneNumber;
    private String customerType;
    private String status;
}