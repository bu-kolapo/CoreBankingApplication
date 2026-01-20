package com.llmassistant.service.model;

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
@Table("llm_conversations")
public class LlmConversation {
    @Id
    private Long id;
    private String conversationId;
    private Long customerId;
    private String query;
    private String response;
    private String model; // GPT-4, GPT-3.5-TURBO
    private String intent; // ACCOUNT_QUERY, TRANSACTION_HISTORY, FRAUD_DETECTION
    private Integer tokensUsed;
    private LocalDateTime createdAt;
}