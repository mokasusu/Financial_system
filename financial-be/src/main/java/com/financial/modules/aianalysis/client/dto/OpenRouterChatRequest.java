package com.financial.modules.aianalysis.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenRouterChatRequest {

    private String model;
    private List<OpenRouterMessage> messages;

    @JsonProperty("response_format")
    private Map<String, String> responseFormat;

    private Double temperature;

    @JsonProperty("max_tokens")
    private Integer maxTokens;
}

