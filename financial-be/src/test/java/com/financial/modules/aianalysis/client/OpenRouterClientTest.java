package com.financial.modules.aianalysis.client;

import com.financial.common.exception.OpenRouterApiException;
import com.financial.common.exception.OpenRouterTimeoutException;
import com.financial.modules.aianalysis.client.dto.OpenRouterChatRequest;
import com.financial.modules.aianalysis.client.dto.OpenRouterChatResponse;
import com.financial.modules.aianalysis.client.dto.OpenRouterMessage;
import com.financial.modules.aianalysis.config.OpenRouterProperties;
import io.netty.handler.timeout.ReadTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenRouterClientTest {

    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private OpenRouterProperties properties;
    private OpenRouterClient client;

    @BeforeEach
    void setUp() {
        properties = new OpenRouterProperties();
        properties.setApiKey("test-openrouter-key");
        properties.setTimeout("10s");
        properties.setModel("openrouter/free");
        client = new OpenRouterClient(webClient, properties);
    }

    private void mockWebClientChain(Mono<OpenRouterChatResponse> responseMono) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/chat/completions")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OpenRouterChatResponse.class)).thenReturn(responseMono);
    }

    @Test
    @DisplayName("generateChatCompletion throws OpenRouterApiException when API key is missing")
    void testGenerateChatCompletion_MissingApiKey() {
        properties.setApiKey("");
        OpenRouterChatRequest request = OpenRouterChatRequest.builder().model("openrouter/free").build();

        assertThatThrownBy(() -> client.generateChatCompletion(request))
                .isInstanceOf(OpenRouterApiException.class)
                .hasMessageContaining("API key is not configured");
    }

    @Test
    @DisplayName("generateChatCompletion returns message content on success")
    void testGenerateChatCompletion_Success() {
        OpenRouterChatResponse mockResponse = OpenRouterChatResponse.builder()
                .id("gen-123")
                .choices(List.of(
                        new OpenRouterChatResponse.Choice(0, new OpenRouterMessage("assistant", "{\"executiveSummary\": \"Good\"}"), "stop")
                ))
                .build();

        mockWebClientChain(Mono.just(mockResponse));

        OpenRouterChatRequest request = OpenRouterChatRequest.builder().model("openrouter/free").build();
        String result = client.generateChatCompletion(request);

        assertThat(result).isEqualTo("{\"executiveSummary\": \"Good\"}");
    }

    @Test
    @DisplayName("generateChatCompletion throws OpenRouterTimeoutException on timeout")
    void testGenerateChatCompletion_Timeout() {
        mockWebClientChain(Mono.error(ReadTimeoutException.INSTANCE));

        OpenRouterChatRequest request = OpenRouterChatRequest.builder().model("openrouter/free").build();

        assertThatThrownBy(() -> client.generateChatCompletion(request))
                .isInstanceOf(OpenRouterTimeoutException.class);
    }

    @Test
    @DisplayName("generateChatCompletion throws OpenRouterApiException when response choices are empty")
    void testGenerateChatCompletion_EmptyChoices() {
        OpenRouterChatResponse mockResponse = OpenRouterChatResponse.builder()
                .id("gen-123")
                .choices(List.of())
                .build();

        mockWebClientChain(Mono.just(mockResponse));

        OpenRouterChatRequest request = OpenRouterChatRequest.builder().model("openrouter/free").build();

        assertThatThrownBy(() -> client.generateChatCompletion(request))
                .isInstanceOf(OpenRouterApiException.class)
                .hasMessageContaining("empty response");
    }
}

