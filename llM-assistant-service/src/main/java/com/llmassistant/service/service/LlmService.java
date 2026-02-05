package com.llmassistant.service.service;

import com.llmassistant.service.dto.request.LlmConversationDto;
import com.llmassistant.service.dto.response.LlmQueryResponseDto;
import com.llmassistant.service.model.LlmRequest;
import com.llmassistant.service.model.LlmResponse;
import reactor.core.publisher.Mono;

public interface LlmService {
    Mono<LlmResponse> processQuery(LlmRequest request);

    Mono<Void> clearConversation(String conversationId);
}














