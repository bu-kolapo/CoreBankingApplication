package com.llmassistant.service.service.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.llmassistant.service.clients.OpenAIClient;
import com.llmassistant.service.model.ConversationContext;
import com.llmassistant.service.model.EnrichedContext;
import com.llmassistant.service.model.LlmRequest;
import com.llmassistant.service.model.LlmResponse;
import com.llmassistant.service.model.entity.ConversationHistory;
import com.llmassistant.service.repository.ConversationRepository;
import com.llmassistant.service.service.ContextEnrichmentService;
import com.llmassistant.service.service.LlmService;
import com.llmassistant.service.service.PromptTemplateService;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmServiceImpl implements LlmService {

    private final OpenAIClient openAIClient;
    private final ContextEnrichmentService contextEnrichmentService;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final PromptTemplateService promptTemplateService;
    private final ConversationRepository conversationRepository;
    private final ReactiveRedisTemplate<String, ConversationContext> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${conversation.max-history}")
    private Integer maxHistory;

    @Value("${conversation.context-window}")
    private Integer contextWindow;

    @Value("${conversation.cache-ttl}")
    private Long cacheTtl;

    @Override
    @CircuitBreaker(name = "llmService", fallbackMethod = "fallbackProcessQuery")
    public Mono<LlmResponse> processQuery(LlmRequest request) {
        String conversationId = request.getConversationId() != null
                ? request.getConversationId()
                : UUID.randomUUID().toString();

        return contextEnrichmentService.enrichContext(request)
                .flatMap(enrichedContext -> buildConversationContext(conversationId, request, enrichedContext))
                .flatMap(this::callOpenAI)
                .flatMap(response -> saveConversation(conversationId, request, response)
                        .thenReturn(response))
                .doOnSuccess(response -> log.info("Successfully processed query for conversation: {}", conversationId))
                .doOnError(error -> log.error("Error processing query for conversation: {}", conversationId, error));
    }

    private Mono<List<ChatMessage>> buildConversationContext(
            String conversationId,
            LlmRequest request,
            EnrichedContext enrichedContext) {

        return getOrCreateConversationContext(conversationId, request)
                .map(context -> {
                    List<ChatMessage> messages = new ArrayList<>();

                    // System prompt
                    LlmRequest.QueryType queryType = request.getQueryType() != null
                            ? request.getQueryType()
                            : LlmRequest.QueryType.GENERAL_ASSISTANCE;

                    messages.add(new ChatMessage(
                            ChatMessageRole.SYSTEM.value(),
                            promptTemplateService.buildSystemPrompt(queryType)
                    ));

                    // Context information
                    String contextPrompt = promptTemplateService.buildContextPrompt(enrichedContext);
                    if (!contextPrompt.isEmpty()) {
                        messages.add(new ChatMessage(
                                ChatMessageRole.SYSTEM.value(),
                                contextPrompt
                        ));
                    }

                    // Conversation history (last N messages)
                    int historySize = Math.min(context.getMessages().size(), contextWindow);
                    List<ConversationContext.Message> recentMessages =
                            context.getMessages().subList(
                                    Math.max(0, context.getMessages().size() - historySize),
                                    context.getMessages().size()
                            );

                    recentMessages.forEach(msg ->
                            messages.add(new ChatMessage(msg.getRole(), msg.getContent()))
                    );

                    // Current user message
                    messages.add(new ChatMessage(
                            ChatMessageRole.USER.value(),
                            request.getUserQuery()
                    ));

                    return messages;
                });
    }

    private Mono<ConversationContext> getOrCreateConversationContext(
            String conversationId,
            LlmRequest request) {

        String redisKey = "conversation:" + conversationId;

        return redisTemplate.opsForValue()
                .get(redisKey)
                .switchIfEmpty(
                        conversationRepository.findRecentByConversationId(conversationId, maxHistory)
                                .map(this::toContextMessage)
                                .collectList()
                                .map(messages -> ConversationContext.builder()
                                        .conversationId(conversationId)
                                        .userId(request.getUserId())
                                        .customerId(request.getCustomerId())
                                        .messages(messages)
                                        .createdAt(LocalDateTime.now())
                                        .lastUpdatedAt(LocalDateTime.now())
                                        .build())
                                .defaultIfEmpty(ConversationContext.builder()
                                        .conversationId(conversationId)
                                        .userId(request.getUserId())
                                        .customerId(request.getCustomerId())
                                        .messages(new ArrayList<>())
                                        .createdAt(LocalDateTime.now())
                                        .lastUpdatedAt(LocalDateTime.now())
                                        .build())
                );
    }

    private ConversationContext.Message toContextMessage(ConversationHistory history) {
        return ConversationContext.Message.builder()
                .role(ChatMessageRole.USER.value())
                .content(history.getUserMessage())
                .timestamp(history.getCreatedAt())
                .build();
    }

    private Mono<LlmResponse> callOpenAI(List<ChatMessage> messages) {
        return openAIClient.createChatCompletion(messages)
                .map(result -> {
                    String responseText = result.getChoices().get(0).getMessage().getContent();
                    long tokensUsed = result.getUsage().getTotalTokens();

                    return LlmResponse.builder()
                            .responseId(UUID.randomUUID().toString())
                            .response(responseText)
                            .tokensUsed(tokensUsed)
                            .timestamp(LocalDateTime.now())
                            .confidence(0.95) // You can implement confidence scoring
                            .build();
                });
    }

    private Mono<Void> saveConversation(
            String conversationId,
            LlmRequest request,
            LlmResponse response) {

        ConversationHistory history = ConversationHistory.builder()
                .conversationId(conversationId)
                .userId(request.getUserId())
                .customerId(request.getCustomerId())
                .userMessage(request.getUserQuery())
                .assistantResponse(response.getResponse())
                .queryType(request.getQueryType() != null ? request.getQueryType().name() : null)
                .tokensUsed(response.getTokensUsed())
                .createdAt(LocalDateTime.now())
                .build();

        try {
            history.setMetadata(objectMapper.writeValueAsString(response.getMetadata()));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize metadata", e);
        }

        return conversationRepository.save(history)
                .then(updateRedisCache(conversationId, request, response))
                .then();
    }

    private Mono<Boolean> updateRedisCache(
            String conversationId,
            LlmRequest request,
            LlmResponse response) {

        String redisKey = "conversation:" + conversationId;

        return getOrCreateConversationContext(conversationId, request)
                .map(context -> {
                    // Add user message
                    context.getMessages().add(ConversationContext.Message.builder()
                            .role(ChatMessageRole.USER.value())
                            .content(request.getUserQuery())
                            .timestamp(LocalDateTime.now())
                            .build());

                    // Add assistant response
                    context.getMessages().add(ConversationContext.Message.builder()
                            .role(ChatMessageRole.ASSISTANT.value())
                            .content(response.getResponse())
                            .timestamp(LocalDateTime.now())
                            .build());

                    context.setLastUpdatedAt(LocalDateTime.now());
                    response.setConversationId(conversationId);

                    return context;
                })
                .flatMap(context ->
                        redisTemplate.opsForValue()
                                .set(redisKey, context, Duration.ofSeconds(cacheTtl))
                );
    }

    @Override
    public Mono<Void> clearConversation(String conversationId) {
        String redisKey = "conversation:" + conversationId;

        return redisTemplate.delete(redisKey)
                .then(conversationRepository.deleteByConversationId(conversationId))
                .then();
    }

    // Fallback method for circuit breaker
    public Mono<LlmResponse> fallbackProcessQuery(LlmRequest request, Exception ex) {
        log.error("Circuit breaker activated. Returning fallback response", ex);

        return Mono.just(LlmResponse.builder()
                .responseId(UUID.randomUUID().toString())
                .response("I apologize, but I'm currently experiencing technical difficulties. " +
                        "Please try again in a moment or contact customer support for immediate assistance.")
                .confidence(0.0)
                .timestamp(LocalDateTime.now())
                .build());
    }
}