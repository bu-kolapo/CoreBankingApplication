package com.customer.service.service;

import com.customer.service.dto.CustomerDto;
import com.customer.service.dto.CustomerRequestDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {

    /**
     * Get customer by ID
     */
    Mono<CustomerDto> getCustomerById(Long customerId);

    /**
     * Get customer by email
     */
    Mono<CustomerDto> getCustomerByEmail(String email);

    /**
     * Create new customer
     */
    Mono<CustomerDto> createCustomer(CustomerRequestDto request);

    /**
     * Update customer KYC status
     */
    Mono<CustomerDto> updateKycStatus(Long customerId, String status);

    /**
     * Search customers
     */
    Flux<CustomerDto> searchCustomers(String searchTerm);
}


