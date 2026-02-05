package com.llmassistant.service.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedContext {

    private AccountInfo accountInfo;
    private List<TransactionInfo> recentTransactions;
    private FraudInfo fraudInfo;
    private CustomerInfo customerInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountInfo {
        private String accountId;
        private String accountNumber;
        private String accountType;
        private BigDecimal balance;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionInfo {
        private String transactionId;
        private String type;
        private BigDecimal amount;
        private String status;
        private String timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FraudInfo {
        private Boolean hasAlerts;
        private List<String> recentAlerts;
        private String riskLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private String customerId;
        private String name;
        private String email;
        private String accountTier;
    }
}