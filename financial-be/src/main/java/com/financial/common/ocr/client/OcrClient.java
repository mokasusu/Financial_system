package com.financial.common.ocr.client;

import com.financial.common.config.OcrProperties;
import com.financial.common.exception.OcrTimeoutException;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
public class OcrClient {

    private static final Logger log = LoggerFactory.getLogger(OcrClient.class);

    private final WebClient webClient;
    private final OcrProperties ocrProperties;

    public OcrClient(@Qualifier("ocrWebClient") WebClient webClient, OcrProperties ocrProperties) {
        this.webClient = webClient;
        this.ocrProperties = ocrProperties;
    }

    /**
     * Executes an asynchronous POST request with multipart form data.
     * Applies the configured response timeout and maps Reactor / Netty timeout exceptions to OcrTimeoutException.
     */
    public <T> Mono<T> postMultipartAsync(String uri, MultiValueMap<String, HttpEntity<?>> multipartData, Class<T> responseType) {
        Duration responseTimeout = ocrProperties.getResponseTimeout() != null
                ? ocrProperties.getResponseTimeout()
                : Duration.ofSeconds(600);

        return webClient.post()
                .uri(uri)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartData))
                .retrieve()
                .bodyToMono(responseType)
                .timeout(responseTimeout)
                .onErrorMap(this::mapTimeoutException);
    }

    /**
     * Executes a synchronous POST request with multipart form data, waiting up to the configured timeout.
     */
    public <T> T postMultipart(String uri, MultiValueMap<String, HttpEntity<?>> multipartData, Class<T> responseType) {
        try {
            return postMultipartAsync(uri, multipartData, responseType).block();
        } catch (Exception ex) {
            throw mapTimeoutException(ex);
        }
    }

    /**
     * Helper to upload a single file resource along with optional form parameters.
     */
    public <T> T uploadFile(String uri, String fileFieldName, Resource fileResource, String filename, Map<String, Object> additionalFields, Class<T> responseType) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        if (filename != null && !filename.isBlank()) {
            builder.part(fileFieldName, fileResource).filename(filename);
        } else {
            builder.part(fileFieldName, fileResource);
        }

        if (additionalFields != null) {
            additionalFields.forEach(builder::part);
        }

        return postMultipart(uri, builder.build(), responseType);
    }

    /**
     * Checks if the throwable or its root cause is a timeout and converts it to OcrTimeoutException.
     * Prevents leaking API credentials or sensitive headers in error messages.
     */
    public RuntimeException mapTimeoutException(Throwable throwable) {
        if (throwable instanceof OcrTimeoutException ocrTimeoutEx) {
            return ocrTimeoutEx;
        }

        if (isTimeout(throwable)) {
            long timeoutSeconds = ocrProperties.getResponseTimeout() != null
                    ? ocrProperties.getResponseTimeout().toSeconds()
                    : 600L;
            log.error("OCR API request timed out after configured {} seconds.", timeoutSeconds);
            return new OcrTimeoutException(timeoutSeconds, throwable);
        }

        if (throwable instanceof RuntimeException runtimeException) {
            return runtimeException;
        }

        return new RuntimeException("Error communicating with OCR service: " + throwable.getMessage(), throwable);
    }

    public boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof TimeoutException ||
                current instanceof ReadTimeoutException ||
                current instanceof ConnectTimeoutException ||
                current instanceof SocketTimeoutException) {
                return true;
            }
            if (current instanceof WebClientRequestException requestEx) {
                if (requestEx.getCause() != null && isTimeout(requestEx.getCause())) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}

