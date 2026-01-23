package com.llmassistant.service.service;

import com.llmassistant.service.dto.request.LlmConversationDto;
import com.llmassistant.service.dto.response.LlmQueryResponseDto;
import reactor.core.publisher.Mono;

public interface LlmService {
    /**
     * Process customer query using LLM
     */
    Mono<LlmQueryResponseDto> processQuery(LlmConversationDto request);

    /**
     * Get conversation history
     */
    Mono<LlmConversationDto> getConversation(String conversationId);

    /**
     * Analyze transaction for insights
     */
    Mono<String> analyzeTransaction(String transactionId);
}
