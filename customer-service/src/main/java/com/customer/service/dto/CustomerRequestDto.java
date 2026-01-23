package com.customer.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
}
