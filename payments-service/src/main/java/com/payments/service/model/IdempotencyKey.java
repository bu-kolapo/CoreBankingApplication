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
    @Table("idempotency_keys")
    public class IdempotencyKey {
        @Id
        private Long id;
        private String idempotencyKey; // Client-provided unique key
        private String requestHash; // Hash of request body
        private String endpoint;
        private String httpMethod;
        private String responseBody;
        private Integer responseStatus;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;
    }




