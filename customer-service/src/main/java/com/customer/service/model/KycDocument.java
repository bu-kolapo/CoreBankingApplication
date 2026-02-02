package com.customer.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("kyc_documents")
public class KycDocument {

    @Id
    private Long id;

    private String documentId;
    private String customerId;

    private String documentType;            // ID_PROOF, ADDRESS_PROOF, SELFIE, etc.
    private String documentCategory;        // PASSPORT, UTILITY_BILL, etc.
    private String documentUrl;             // S3 or file storage URL
    private String documentNumber;

    private String verificationStatus;      // PENDING, APPROVED, REJECTED
    private String verifiedBy;              // Admin/System ID
    private String rejectionReason;

    private LocalDateTime uploadedAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}