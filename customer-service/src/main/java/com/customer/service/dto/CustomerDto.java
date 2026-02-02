package com.customer.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {
    private String customerId;
    private String customerNumber;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    private LocalDate dateOfBirth;
    private String gender;
    private String nationality;

    private String status;
    private String kycStatus;
    private String kycLevel;

    private String accountType;
    private String customerSegment;
    private String customerType;
    private String riskRating;

    private Boolean emailVerified;
    private Boolean phoneVerified;

    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
