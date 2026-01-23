package com.llmassistant.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmConversationDto {
    private String conversationId;
    private Long customerId;
    private String query;
    private String response;
    private LocalDateTime createdAt;
}

