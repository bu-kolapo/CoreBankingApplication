package com.llmassistant.service.model;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {

    private String responseId;
    private String conversationId;
    private String response;
    private Double confidence;
    private List<String> suggestedActions;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    private long tokensUsed;
}