package com.customer.service.repository;

import com.customer.service.model.Customer;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRepository extends ReactiveCrudRepository<Customer, Long> {

    Mono<Customer> findByCustomerId(String customerId);

    Mono<Customer> findByCustomerNumber(String customerNumber);

    Mono<Customer> findByEmail(String email);

    Mono<Customer> findByPhoneNumber(String phoneNumber);

    Mono<Boolean> existsByEmail(String email);

    Mono<Boolean> existsByPhoneNumber(String phoneNumber);

    Flux<Customer> findByStatus(String status);

    Flux<Customer> findByKycStatus(String kycStatus);

    @Query("SELECT * FROM customers WHERE email = :email OR phone_number = :phoneNumber")
    Mono<Customer> findByEmailOrPhone(String email, String phoneNumber);

    @Query("SELECT COUNT(*) FROM customers WHERE status = 'ACTIVE'")
    Mono<Long> countActiveCustomers();
}