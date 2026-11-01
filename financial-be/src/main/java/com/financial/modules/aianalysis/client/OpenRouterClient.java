package com.financial.modules.aianalysis.client;

import com.financial.common.exception.OpenRouterApiException;
import com.financial.common.exception.OpenRouterTimeoutException;
import com.financial.modules.aianalysis.client.dto.OpenRouterChatRequest;
import com.financial.modules.aianalysis.client.dto.OpenRouterChatResponse;
import com.financial.modules.aianalysis.config.OpenRouterProperties;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class OpenRouterClient {

    private final WebClient webClient;
    private final OpenRouterProperties properties;

    public OpenRouterClient(
            @Qualifier("openRouterWebClient") WebClient webClient,
            OpenRouterProperties properties
    ) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public String generateChatCompletion(OpenRouterChatRequest request) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.trim().isBlank()) {
            log.error("OpenRouter API key is missing. Unable to execute AI financial analysis.");
            throw new OpenRouterApiException("OPENROUTER_API_KEY_MISSING", "OpenRouter API key is not configured.", 500);
        }

        Duration timeout = properties.getTimeout() != null
                ? properties.getTimeout()
                : Duration.ofSeconds(120);

        try {
            log.info("Sending chat completion request to OpenRouter model: {}", request.getModel());

            OpenRouterChatResponse response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        int status = clientResponse.statusCode().value();
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("OpenRouter API error response body empty")
                                .flatMap(errorBody -> {
                                    log.error("OpenRouter API returned HTTP {}: {}", status, errorBody);
                                    if (status == 429) {
                                        return Mono.error(new OpenRouterApiException("OPENROUTER_RATE_LIMIT", "OpenRouter rate limit reached. Please retry later.", 429));
                                    } else if (status >= 500) {
                                        return Mono.error(new OpenRouterApiException("OPENROUTER_SERVER_ERROR", "OpenRouter server error (HTTP " + status + ").", 502));
                                    } else {
                                        return Mono.error(new OpenRouterApiException("OPENROUTER_CLIENT_ERROR", "OpenRouter request rejected (HTTP " + status + ").", status));
                                    }
                                });
                    })
                    .bodyToMono(OpenRouterChatResponse.class)
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                            .filter(throwable -> {
                                if (throwable instanceof OpenRouterApiException apiEx) {
                                    return apiEx.getStatusCode() == 429 || apiEx.getStatusCode() >= 500;
                                }
                                return false;
                            })
                            .doBeforeRetry(retrySignal -> log.warn("Retrying OpenRouter API call due to transient error. Attempt: {}", retrySignal.totalRetries() + 1))
                    )
                    .onErrorMap(this::mapTimeoutException)
                    .block();

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                if (response != null && response.getError() != null) {
                    throw new OpenRouterApiException("OPENROUTER_ERROR", "OpenRouter returned error: " + response.getError().getMessage(), 502);
                }
                throw new OpenRouterApiException("OPENROUTER_EMPTY_RESPONSE", "OpenRouter returned an empty response.", 502);
            }

            String content = response.getChoices().get(0).getMessage() != null
                    ? response.getChoices().get(0).getMessage().getContent()
                    : null;

            if (content == null || content.isBlank()) {
                throw new OpenRouterApiException("OPENROUTER_EMPTY_CONTENT", "OpenRouter response choice message content is empty.", 502);
            }

            return content;
        } catch (WebClientResponseException ex) {
            log.error("OpenRouter WebClientResponseException: HTTP {}", ex.getStatusCode().value());
            throw new OpenRouterApiException("OPENROUTER_HTTP_ERROR", "OpenRouter HTTP error: " + ex.getStatusCode().value(), ex.getStatusCode().value(), ex);
        } catch (Exception ex) {
            throw mapTimeoutException(ex);
        }
    }

    private RuntimeException mapTimeoutException(Throwable throwable) {
        if (throwable instanceof OpenRouterTimeoutException timeoutEx) {
            return timeoutEx;
        }
        if (throwable instanceof OpenRouterApiException apiEx) {
            return apiEx;
        }

        if (isTimeout(throwable)) {
            long timeoutSec = properties.getTimeout() != null ? properties.getTimeout().toSeconds() : 120L;
            log.error("OpenRouter request timed out after {} seconds.", timeoutSec);
            return new OpenRouterTimeoutException(timeoutSec, throwable);
        }

        return new OpenRouterApiException("OPENROUTER_REQUEST_FAILED", "Failed to communicate with OpenRouter: " + throwable.getMessage(), 502, throwable);
    }

    private boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof TimeoutException
                    || current instanceof ReadTimeoutException
                    || current instanceof ConnectTimeoutException
                    || current instanceof SocketTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}

