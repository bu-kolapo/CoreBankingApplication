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
@Table("audit_logs")
public class AuditLog {
    @Id
    private Long id;
    private String entityType; // PAYMENT, TRANSACTION, ACCOUNT
    private Long entityId;
    private String action; // CREATE, UPDATE, DELETE
    private String changedBy;
    private String oldValue; // JSON
    private String newValue; // JSON
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}