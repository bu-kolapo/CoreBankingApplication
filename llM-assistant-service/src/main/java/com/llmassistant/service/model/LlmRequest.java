package com.llmassistant.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmRequest {

    @NotBlank(message = "User query is required")
    private String userQuery;

    @NotNull(message = "User ID is required")
    private String userId;

    private String customerId;

    private String conversationId;

    private QueryType queryType;

    private Map<String, Object> additionalContext;

    public enum QueryType {
        ACCOUNT_INQUIRY,
        TRANSACTION_QUERY,
        FRAUD_ALERT,
        PAYMENT_STATUS,
        DISPUTE_ASSISTANCE,
        GENERAL_ASSISTANCE,
        RECONCILIATION_ISSUE
    }
}