package com.llmassistant.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationContext {

    private String conversationId;
    private String userId;
    private String customerId;

    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role; // "user" or "assistant"
        private String content;
        private LocalDateTime timestamp;
    }
}