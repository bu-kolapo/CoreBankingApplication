package com.payments.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("api_keys")
public class ApiKey {
    @Id
    private Long id;
    private String keyHash;
    private String keyPrefix;
    private String name;
    private Long customerId;
    private String[] permissions;
    private String status; // ACTIVE, REVOKED, EXPIRED
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}