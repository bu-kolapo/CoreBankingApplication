package com.llmassistant.service.controller;


import com.llmassistant.service.model.LlmRequest;
import com.llmassistant.service.model.LlmResponse;
import com.llmassistant.service.service.LlmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmController {

    private final LlmService llmService;

    @PostMapping("/query")
    public Mono<ResponseEntity<LlmResponse>> processQuery(@Valid @RequestBody LlmRequest request) {
        log.info("Received LLM query from user: {}", request.getUserId());

        return llmService.processQuery(request)
                .map(response -> ResponseEntity.ok(response))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .onErrorResume(error -> {
                    log.error("Error processing query", error);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @DeleteMapping("/conversation/{conversationId}")
    public Mono<ResponseEntity<Void>> clearConversation(@PathVariable String conversationId) {
        log.info("Clearing conversation: {}", conversationId);

        return llmService.clearConversation(conversationId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(error -> {
                    log.error("Error clearing conversation", error);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<String>> health() {
        return Mono.just(ResponseEntity.ok("LLM Assistance Service is running"));
    }
}
