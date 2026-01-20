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
@Table("payment_retries")
public class PaymentRetry {
    @Id
    private Long id;
    private Long paymentId;
    private Integer attemptNumber;
    private String status; // SCHEDULED, PROCESSING, SUCCESS, FAILED
    private String failureReason;
    private LocalDateTime scheduledAt;
    private LocalDateTime attemptedAt;
    private LocalDateTime createdAt;
}
