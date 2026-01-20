package com.llmassistant.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmQueryRequestDto {
    @NotBlank
    private String query;

    @NotNull
    private Long customerId;

    private String context;
    private String conversationId;
}

