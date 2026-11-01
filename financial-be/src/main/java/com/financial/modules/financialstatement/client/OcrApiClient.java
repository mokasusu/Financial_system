package com.financial.modules.financialstatement.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.config.OcrProperties;
import com.financial.common.exception.OcrApiException;
import com.financial.common.exception.OcrTimeoutException;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
public class OcrApiClient {

    private static final Logger log = LoggerFactory.getLogger(OcrApiClient.class);

    private final WebClient webClient;
    private final OcrProperties ocrProperties;
    private final ObjectMapper objectMapper;

    @Autowired
    public OcrApiClient(
            @Qualifier("ocrWebClient") WebClient webClient,
            OcrProperties ocrProperties,
            ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.ocrProperties = ocrProperties;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    public OcrApiClient(
            @Qualifier("ocrWebClient") WebClient webClient,
            OcrProperties ocrProperties) {
        this(webClient, ocrProperties, new ObjectMapper());
    }

    /**
     * Calls external OCR API POST /api/v1/analyze with multipart file and parameters.
     * Enforces a total operation timeout of 600s and translates timeouts to OcrTimeoutException.
     *
     * @param fileBytes file content bytes
     * @param fileName original file name
     * @param period reporting period ("current" or "previous")
     * @param enableAi whether AI analysis is enabled (default true)
     * @param storeImages whether evidence images should be stored (default true)
     * @return raw JSON response string
     */
    public String analyzeFinancialStatement(
            byte[] fileBytes,
            String fileName,
            String period,
            Boolean enableAi,
            Boolean storeImages) {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        builder.part("file", fileResource).filename(fileName);

        if (period != null && !period.isBlank()) {
            builder.part("period", period);
        } else {
            builder.part("period", "current");
        }

        builder.part("enable_ai", enableAi != null ? enableAi.toString() : "true");
        builder.part("store_images", storeImages != null ? storeImages.toString() : "true");

        Duration timeout = ocrProperties.getResponseTimeout() != null
                ? ocrProperties.getResponseTimeout()
                : Duration.ofSeconds(600);

        try {
            log.info("Sending OCR extraction request for file '{}' (size: {} bytes) with timeout: {}s",
                    fileName, fileBytes.length, timeout.toSeconds());

            String responseBody = webClient.post()
                    .uri("/api/v1/extract")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        int code = clientResponse.statusCode().value();
                        if (code == 401) {
                            return Mono.error(new OcrApiException("OCR_UNAUTHORIZED", "OCR API authorization failed. Check X-API-Key.", 401));
                        }
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("OCR client error")
                                .flatMap(msg -> Mono.error(new OcrApiException("OCR_ANALYSIS_FAILED", "OCR analysis rejected: " + msg, code)));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, serverResponse -> {
                        int code = serverResponse.statusCode().value();
                        if (code == 503) {
                            return Mono.error(new OcrApiException("OCR_SERVER_BUSY", "OCR service is currently unavailable or busy.", 503));
                        }
                        return serverResponse.bodyToMono(String.class)
                                .defaultIfEmpty("OCR server error")
                                .flatMap(msg -> Mono.error(new OcrApiException("OCR_ANALYSIS_FAILED", "OCR server failed: " + msg, code)));
                    })
                    .bodyToMono(String.class)
                    .timeout(timeout)
                    .block();

            if (responseBody == null || responseBody.isBlank()) {
                throw new OcrApiException("OCR_INVALID_RESPONSE", "Empty response received from OCR service.");
            }

            // Validate that response is valid JSON
            try {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                if (jsonNode.has("success") && !jsonNode.get("success").asBoolean(true)) {
                    String err = jsonNode.has("message") ? jsonNode.get("message").asText() : "OCR processing failed";
                    throw new OcrApiException("OCR_ANALYSIS_FAILED", err);
                }
            } catch (IOException e) {
                throw new OcrApiException("OCR_INVALID_RESPONSE", "Invalid JSON received from OCR service: " + e.getMessage(), e);
            }

            return responseBody;

        } catch (Exception ex) {
            throw handleOcrException(ex, timeout.toSeconds());
        }
    }

    private RuntimeException handleOcrException(Exception ex, long timeoutSeconds) {
        if (ex instanceof OcrTimeoutException ocrTimeoutEx) {
            return ocrTimeoutEx;
        }
        if (ex instanceof OcrApiException ocrApiEx) {
            return ocrApiEx;
        }

        if (isTimeout(ex)) {
            log.error("OCR request timed out after {} seconds.", timeoutSeconds);
            return new OcrTimeoutException(timeoutSeconds, ex);
        }

        if (ex instanceof WebClientResponseException responseEx) {
            int code = responseEx.getStatusCode().value();
            if (code == 401) {
                return new OcrApiException("OCR_UNAUTHORIZED", "OCR API authorization failed.", 401);
            }
            if (code == 503) {
                return new OcrApiException("OCR_SERVER_BUSY", "OCR server is busy or unavailable.", 503);
            }
            return new OcrApiException("OCR_ANALYSIS_FAILED", "OCR API failed: " + responseEx.getResponseBodyAsString(), code);
        }

        return new OcrApiException("OCR_ANALYSIS_FAILED", "Error communicating with OCR service: " + ex.getMessage(), ex);
    }

    private boolean isTimeout(Throwable throwable) {
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

