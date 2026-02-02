package com.account.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotBlank(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Account type is required")
    private String accountType;         // SAVINGS, CHECKING, WALLET

    @NotBlank(message = "Currency is required")
    private String currency;

    private BigDecimal initialDeposit;

    private BigDecimal dailyLimit;

    private BigDecimal monthlyLimit;
}
