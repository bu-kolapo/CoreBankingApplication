package com.customer.service.service;

import com.customer.service.dto.CustomerDto;
import com.customer.service.dto.CustomerRequestDto;
import com.customer.service.dto.KycSubmissionRequest;
import com.customer.service.model.CustomerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {

    // Customer profile management
    Mono<CustomerDto> createCustomer(CustomerRequest request);

    Mono<CustomerDto> getCustomer(String customerId);

    Mono<CustomerDto> getCustomerByEmail(String email);

    Mono<CustomerDto> updateCustomer(String customerId, CustomerRequest request);

    // KYC management
    Mono<Void> submitKyc(KycSubmissionRequest request);

    Mono<Void> verifyKyc(String customerId, boolean approved, String rejectionReason);

    Mono<String> getKycStatus(String customerId);

    // Verification
    Mono<Void> verifyEmail(String customerId, String verificationCode);

    Mono<Void> verifyPhone(String customerId, String verificationCode);

    // Status management
    Mono<Void> suspendCustomer(String customerId, String reason);

    Mono<Void> activateCustomer(String customerId);

    // Authentication
    Mono<CustomerDto> authenticate(String email, String password);

    Mono<Void> changePassword(String customerId, String oldPassword, String newPassword);
}


