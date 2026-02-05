package com.llmassistant.service.repository;


import com.llmassistant.service.model.entity.ConversationHistory;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ConversationRepository extends ReactiveCrudRepository<ConversationHistory, Long> {

    Flux<ConversationHistory> findByConversationIdOrderByCreatedAtDesc(String conversationId);

    Flux<ConversationHistory> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT * FROM conversation_history WHERE conversation_id = :conversationId " +
            "ORDER BY created_at DESC LIMIT :limit")
    Flux<ConversationHistory> findRecentByConversationId(String conversationId, int limit);

    Mono<Void> deleteByConversationId(String conversationId);
}