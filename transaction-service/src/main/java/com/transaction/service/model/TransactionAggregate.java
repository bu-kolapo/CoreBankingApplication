package com.transaction.service.model;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAggregate {
    private String accountId;
    private LocalDate date;
    private String currency;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal netAmount;
    private Long transactionCount;
}