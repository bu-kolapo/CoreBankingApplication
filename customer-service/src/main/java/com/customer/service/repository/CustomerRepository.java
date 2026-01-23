package com.customer.service.repository;

import com.customer.service.model.Customer;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRepository extends R2dbcRepository<Customer, Long> {
    Mono<Customer> findByEmail(String email);
    Mono<Customer> findByPhoneNumber(String phoneNumber);
    Mono<Customer> findByIdentificationNumber(String identificationNumber);
    Flux<Customer> findByKycStatus(String kycStatus);

    @Query("SELECT * FROM customers WHERE LOWER(first_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(last_name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Flux<Customer> searchCustomers(String searchTerm);
}
