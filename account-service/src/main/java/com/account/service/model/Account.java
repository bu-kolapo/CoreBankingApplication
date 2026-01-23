package com.account.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("accounts")
public class Account {
    @Id
    private Long id;
    private String accountNumber;
    private String accountType; // SAVINGS, CURRENT, CREDIT
    private BigDecimal balance;
    private String currency;
    private Long customerId;
    private String status; // ACTIVE, SUSPENDED, CLOSED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}