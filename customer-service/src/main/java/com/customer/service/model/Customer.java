package com.customer.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("customers")
public class Customer {

    @Id
    private Long id;

    private String customerId;              // UUID - our internal ID
    private String customerNumber;
    private String customerType;// Customer-facing number (e.g., CUST-000001)

    // Personal Information
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String phoneNumber;
    private String alternativePhone;

    private LocalDate dateOfBirth;
    private String gender;                  // MALE, FEMALE, OTHER
    private String nationality;

    // Address Information
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    // KYC Information
    private String kycStatus;               // PENDING, VERIFIED, REJECTED, EXPIRED
    private String kycLevel;                // TIER_1, TIER_2, TIER_3
    private String identificationType;      // PASSPORT, NATIONAL_ID, DRIVERS_LICENSE
    private String identificationNumber;
    private LocalDate identificationExpiry;
    private String identificationDocumentUrl;

    // Account Status
    private String status;                  // ACTIVE, INACTIVE, SUSPENDED, CLOSED
    private String accountType;             // INDIVIDUAL, BUSINESS
    private String customerSegment;         // RETAIL, PREMIUM, CORPORATE

    // Risk & Compliance
    private String riskRating;              // LOW, MEDIUM, HIGH
    private Boolean pepStatus;              // Politically Exposed Person
    private Boolean sanctionScreening;

    // Authentication
    private String passwordHash;
    private String salt;
    private Boolean twoFactorEnabled;
    private String twoFactorSecret;

    // Preferences
    private String preferredLanguage;
    private String preferredCurrency;
    private Boolean marketingConsent;
    private Boolean emailNotifications;
    private Boolean smsNotifications;

    // Metadata
    private String referralCode;
    private String referredBy;
    private String metadata;                // JSON for additional fields

    // Audit fields
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime phoneVerifiedAt;
    private LocalDateTime kycVerifiedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}