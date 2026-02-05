package com.llmassistant.service.clients;


import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class OpenAIClient {

    private final OpenAiService openAiService;
    private final String model;
    private final Integer maxTokens;
    private final Double temperature;

    public OpenAIClient(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model,
            @Value("${openai.max-tokens}") Integer maxTokens,
            @Value("${openai.temperature}") Double temperature,
            @Value("${openai.timeout}") Long timeout) {

        this.openAiService = new OpenAiService(apiKey, Duration.ofMillis(timeout));
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
    }

    public Mono<ChatCompletionResult> createChatCompletion(List<ChatMessage> messages) {
        return Mono.fromCallable(() -> {
                    ChatCompletionRequest request = ChatCompletionRequest.builder()
                            .model(model)
                            .messages(messages)
                            .maxTokens(maxTokens)
                            .temperature(temperature)
                            .build();

                    return openAiService.createChatCompletion(request);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> log.error("Error calling OpenAI API", error));
    }
}