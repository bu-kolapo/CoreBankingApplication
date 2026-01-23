package com.llmassistant.service.repository;

import com.llmassistant.service.model.LlmConversation;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface LlmConversationRepository extends R2dbcRepository<LlmConversation, Long> {
    Mono<LlmConversation> findByConversationId(String conversationId);
    Flux<LlmConversation> findByCustomerId(Long customerId);

    @Query("SELECT * FROM llm_conversations WHERE customer_id = :customerId " +
            "ORDER BY created_at DESC LIMIT :limit")
    Flux<LlmConversation> findRecentConversations(Long customerId, int limit);
}