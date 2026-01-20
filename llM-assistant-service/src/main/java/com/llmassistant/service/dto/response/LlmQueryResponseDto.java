package com.llmassistant.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmQueryResponseDto {
    private String conversationId;
    private String response;
    private String intent;
    private Object extractedData;
    private String[] suggestedActions;
    private LocalDateTime timestamp;
}