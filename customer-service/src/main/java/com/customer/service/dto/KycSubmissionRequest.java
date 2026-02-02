package com.customer.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycSubmissionRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Identification type is required")
    private String identificationType;      // PASSPORT, NATIONAL_ID, etc.

    @NotBlank(message = "Identification number is required")
    private String identificationNumber;

    private String identificationDocumentUrl;
    private String addressProofUrl;
    private String selfieUrl;
}
