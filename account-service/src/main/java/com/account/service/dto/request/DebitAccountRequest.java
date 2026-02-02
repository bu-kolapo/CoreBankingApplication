package com.account.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitAccountRequest {
    private String accountId;
    private BigDecimal amount;
    private String reference;
    private String description;
}
