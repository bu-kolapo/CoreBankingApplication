package com.customer.service.service.Implementation;


import com.customer.service.dto.CustomerDto;
import com.customer.service.dto.KycSubmissionRequest;
import com.customer.service.events.CustomerCreatedEvent;
import com.customer.service.events.KycStatusChangedEvent;
import com.customer.service.model.Customer;
import com.customer.service.model.CustomerRequest;
import com.customer.service.repository.CustomerRepository;
import com.customer.service.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @Transactional
    public Mono<CustomerDto> createCustomer(CustomerRequest request) {
        log.info("👤 Creating new customer: {}", request.getEmail());

        return customerRepository.existsByEmail(request.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Customer with this email already exists"));
                    }
                    return customerRepository.existsByPhoneNumber(request.getPhoneNumber());
                })
                .flatMap(phoneExists -> {
                    if (phoneExists) {
                        return Mono.error(new RuntimeException("Customer with this phone number already exists"));
                    }
                    return createNewCustomer(request);
                });
    }

    private Mono<CustomerDto> createNewCustomer(CustomerRequest request) {
        String customerId = UUID.randomUUID().toString();
        String customerNumber = generateCustomerNumber();
        String passwordHash = passwordEncoder.encode(request.getPassword());

        Customer customer = Customer.builder()
                .customerId(customerId)
                .customerNumber(customerNumber)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .middleName(request.getMiddleName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .nationality(request.getNationality())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .status("ACTIVE")
                .customerType(request.getCustomerType() != null ? request.getCustomerType() : "INDIVIDUAL")
                .customerSegment("RETAIL")
                .kycStatus("PENDING")
                .kycLevel("TIER_0")
                .riskRating("LOW")
                .pepStatus(false)
                .passwordHash(passwordHash)
                .twoFactorEnabled(false)
                .preferredLanguage("en")
                .preferredCurrency(request.getPreferredCurrency() != null ? request.getPreferredCurrency() : "USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return customerRepository.save(customer)
                .doOnSuccess(savedCustomer -> {
                    log.info("✅ Customer created: {} ({})", savedCustomer.getCustomerNumber(), savedCustomer.getCustomerId());
                    publishCustomerCreatedEvent(savedCustomer);
                })
                .map(this::toDto);
    }

    @Override
    public Mono<CustomerDto> getCustomer(String customerId) {
        return customerRepository.findByCustomerId(customerId)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new RuntimeException("Customer not found")));
    }

    @Override
    public Mono<CustomerDto> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new RuntimeException("Customer not found")));
    }

    @Override
    @Transactional
    public Mono<Void> submitKyc(KycSubmissionRequest request) {
        log.info("📝 Submitting KYC for customer: {}", request.getCustomerId());

        return customerRepository.findByCustomerId(request.getCustomerId())
                .switchIfEmpty(Mono.error(new RuntimeException("Customer not found")))
                .flatMap(customer -> {
                    customer.setIdentificationType(request.getIdentificationType());
                    customer.setIdentificationNumber(request.getIdentificationNumber());
                    customer.setIdentificationDocumentUrl(request.getIdentificationDocumentUrl());
                    customer.setKycStatus("SUBMITTED");
                    customer.setUpdatedAt(LocalDateTime.now());

                    return customerRepository.save(customer);
                })
                .then()
                .doOnSuccess(v -> log.info("✅ KYC submitted for: {}", request.getCustomerId()));
    }

    @Override
    @Transactional
    public Mono<Void> verifyKyc(String customerId, boolean approved, String rejectionReason) {
        log.info("🔍 Verifying KYC for customer: {}", customerId);

        return customerRepository.findByCustomerId(customerId)
                .switchIfEmpty(Mono.error(new RuntimeException("Customer not found")))
                .flatMap(customer -> {
                    String previousStatus = customer.getKycStatus();

                    if (approved) {
                        customer.setKycStatus("VERIFIED");
                        customer.setKycLevel("TIER_2");
                        customer.setKycVerifiedAt(LocalDateTime.now());
                    } else {
                        customer.setKycStatus("REJECTED");
                    }

                    customer.setUpdatedAt(LocalDateTime.now());

                    return customerRepository.save(customer)
                            .doOnSuccess(updated -> {
                                publishKycStatusChangedEvent(updated, previousStatus);
                            });
                })
                .then()
                .doOnSuccess(v -> log.info("✅ KYC {} for: {}", approved ? "approved" : "rejected", customerId));
    }

    @Override
    public Mono<String> getKycStatus(String customerId) {
        return customerRepository.findByCustomerId(customerId)
                .map(Customer::getKycStatus)
                .switchIfEmpty(Mono.error(new RuntimeException("Customer not found")));
    }

    @Override
    public Mono<Void> verifyEmail(String customerId, String verificationCode) {
        return customerRepository.findByCustomerId(customerId)
                .flatMap(customer -> {
                    customer.setEmailVerifiedAt(LocalDateTime.now());
                    customer.setUpdatedAt(LocalDateTime.now());
                    return customerRepository.save(customer);
                })
                .then();
    }

    @Override
    public Mono<Void> verifyPhone(String customerId, String verificationCode) {
        return customerRepository.findByCustomerId(customerId)
                .flatMap(customer -> {
                    customer.setPhoneVerifiedAt(LocalDateTime.now());
                    customer.setUpdatedAt(LocalDateTime.now());
                    return customerRepository.save(customer);
                })
                .then();
    }

    @Override
    public Mono<Void> suspendCustomer(String customerId, String reason) {
        return customerRepository.findByCustomerId(customerId)
                .flatMap(customer -> {
                    customer.setStatus("SUSPENDED");
                    customer.setUpdatedAt(LocalDateTime.now());
                    return customerRepository.save(customer);
                })
                .then()
                .doOnSuccess(v -> log.info("⚠️ Customer suspended: {}", customerId));
    }

    @Override
    public Mono<Void> activateCustomer(String customerId) {
        return customerRepository.findByCustomerId(customerId)
                .flatMap(customer -> {
                    customer.setStatus("ACTIVE");
                    customer.setUpdatedAt(LocalDateTime.now());
                    return customerRepository.save(customer);
                })
                .then()
                .doOnSuccess(v -> log.info("✅ Customer activated: {}", customerId));
    }

    @Override
    public Mono<CustomerDto> authenticate(String email, String password) {
        return customerRepository.findByEmail(email)
                .filter(customer -> passwordEncoder.matches(password, customer.getPasswordHash()))
                .map(customer -> {
                    customer.setLastLoginAt(LocalDateTime.now());
                    customerRepository.save(customer).subscribe();
                    return customer;
                })
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")));
    }

    @Override
    public Mono<Void> changePassword(String customerId, String oldPassword, String newPassword) {
        return customerRepository.findByCustomerId(customerId)
                .filter(customer -> passwordEncoder.matches(oldPassword, customer.getPasswordHash()))
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid old password")))
                .flatMap(customer -> {
                    customer.setPasswordHash(passwordEncoder.encode(newPassword));
                    customer.setUpdatedAt(LocalDateTime.now());
                    return customerRepository.save(customer);
                })
                .then();
    }

    // Helper methods
    private String generateCustomerNumber() {
        return "CUST-" + System.currentTimeMillis();
    }

    private void publishCustomerCreatedEvent(Customer customer) {
        CustomerCreatedEvent event = CustomerCreatedEvent.builder()
                .customerId(customer.getCustomerId())
                .customerNumber(customer.getCustomerNumber())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .customerType(customer.getCustomerType())
                .status(customer.getStatus())
                .preferredCurrency(customer.getPreferredCurrency())
                .build();

        try {
            kafkaTemplate.send("customer-created", customer.getCustomerId(), event);
            log.info("📤 Published customer created event: {}", customer.getCustomerId());
        } catch (Exception e) {
            log.error("❌ Failed to publish customer created event", e);
        }
    }

    private void publishKycStatusChangedEvent(Customer customer, String previousStatus) {
        KycStatusChangedEvent event = KycStatusChangedEvent.builder()
                .customerId(customer.getCustomerId())
                .kycStatus(customer.getKycStatus())
                .kycLevel(customer.getKycLevel())
                .previousStatus(previousStatus)
                .build();

        try {
            kafkaTemplate.send("kyc-status-changed", customer.getCustomerId(), event);
            log.info("📤 Published KYC status changed event: {}", customer.getCustomerId());
        } catch (Exception e) {
            log.error("❌ Failed to publish KYC status changed event", e);
        }
    }

    private CustomerDto toDto(Customer customer) {
        return CustomerDto.builder()
                .customerId(customer.getCustomerId())
                .customerNumber(customer.getCustomerNumber())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .dateOfBirth(customer.getDateOfBirth())
                .gender(customer.getGender())
                .nationality(customer.getNationality())
                .status(customer.getStatus())
                .kycStatus(customer.getKycStatus())
                .kycLevel(customer.getKycLevel())
                .customerType(customer.getCustomerType())
                .customerSegment(customer.getCustomerSegment())
                .riskRating(customer.getRiskRating())
                .emailVerified(customer.getEmailVerifiedAt() != null)
                .phoneVerified(customer.getPhoneVerifiedAt() != null)
                .createdAt(customer.getCreatedAt())
                .lastLoginAt(customer.getLastLoginAt())
                .build();
    }

    @Override
    public Mono<CustomerDto> updateCustomer(String customerId, CustomerRequest request) {
        return customerRepository.findByCustomerId(customerId)
                .flatMap(customer -> {
                    if (request.getFirstName() != null) customer.setFirstName(request.getFirstName());
                    if (request.getLastName() != null) customer.setLastName(request.getLastName());
                    if (request.getPhoneNumber() != null) customer.setPhoneNumber(request.getPhoneNumber());
                    if (request.getAddressLine1() != null) customer.setAddressLine1(request.getAddressLine1());
                    if (request.getCity() != null) customer.setCity(request.getCity());
                    if (request.getState() != null) customer.setState(request.getState());
                    if (request.getCountry() != null) customer.setCountry(request.getCountry());
                    if (request.getPostalCode() != null) customer.setPostalCode(request.getPostalCode());

                    customer.setUpdatedAt(LocalDateTime.now());

                    return customerRepository.save(customer);
                })
                .map(this::toDto);
    }
}