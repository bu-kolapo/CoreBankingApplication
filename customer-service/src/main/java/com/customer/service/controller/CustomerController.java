package com.customer.service.controller;




import com.customer.service.dto.CustomerAccountDto;
import com.customer.service.dto.CustomerDto;
import com.customer.service.dto.KycSubmissionRequest;
import com.customer.service.model.CustomerRequest;
import com.customer.service.service.CustomerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public Mono<ResponseEntity<CustomerDto>> createCustomer(
            @Valid @RequestBody CustomerRequest request) {

        log.info("📝 Received create customer request: {}", request.getEmail());

        return customerService.createCustomer(request)
                .map(customer -> ResponseEntity.status(HttpStatus.CREATED).body(customer))
                .onErrorResume(error -> {
                    log.error("❌ Failed to create customer", error);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @GetMapping("/{customerId}")
    public Mono<ResponseEntity<CustomerDto>> getCustomer(@PathVariable String customerId) {
        return customerService.getCustomer(customerId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public Mono<ResponseEntity<CustomerDto>> getCustomerByEmail(@PathVariable String email) {
        return customerService.getCustomerByEmail(email)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }



    @PostMapping("/kyc/submit")
    public Mono<ResponseEntity<Void>> submitKyc(
            @Valid @RequestBody KycSubmissionRequest request) {

        return customerService.submitKyc(request)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(error -> {
                    log.error("❌ Failed to submit KYC", error);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @PostMapping("/kyc/{customerId}/verify")
    public Mono<ResponseEntity<Void>> verifyKyc(
            @PathVariable String customerId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectionReason) {

        return customerService.verifyKyc(customerId, approved, rejectionReason)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(error -> {
                    log.error("❌ Failed to verify KYC", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @GetMapping("/kyc/{customerId}/status")
    public Mono<ResponseEntity<String>> getKycStatus(@PathVariable String customerId) {
        return customerService.getKycStatus(customerId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{customerId}/suspend")
    public Mono<ResponseEntity<Void>> suspendCustomer(
            @PathVariable String customerId,
            @RequestParam String reason) {

        return customerService.suspendCustomer(customerId, reason)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @PostMapping("/{customerId}/activate")
    public Mono<ResponseEntity<Void>> activateCustomer(@PathVariable String customerId) {
        return customerService.activateCustomer(customerId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
}