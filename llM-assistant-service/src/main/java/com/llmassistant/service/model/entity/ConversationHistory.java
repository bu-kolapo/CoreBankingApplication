package com.llmassistant.service.model.entity;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("conversation_history")
public class ConversationHistory {

    @Id
    private Long id;

    @Column("conversation_id")
    private String conversationId;

    @Column("user_id")
    private String userId;

    @Column("customer_id")
    private String customerId;

    @Column("user_message")
    private String userMessage;

    @Column("assistant_response")
    private String assistantResponse;

    @Column("query_type")
    private String queryType;

    @Column("tokens_used")
    private long tokensUsed;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("metadata")
    private String metadata; // JSON string
}
