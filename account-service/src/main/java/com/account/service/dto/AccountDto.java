package com.account.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    private String accountId;
    private String accountNumber;
    private String customerId;
    private String accountType;
    private String currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal blockedAmount;
    private String status;
    private BigDecimal dailyTransactionLimit;
}
