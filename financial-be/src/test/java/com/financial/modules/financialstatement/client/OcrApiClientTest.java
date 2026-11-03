package com.financial.modules.financialstatement.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.config.OcrProperties;
import com.financial.common.exception.OcrApiException;
import com.financial.common.exception.OcrTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OcrApiClientTest {

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

    private OcrProperties ocrProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private OcrApiClient ocrApiClient;

    @BeforeEach
    void setUp() {
        ocrProperties = new OcrProperties();
        ocrProperties.setBaseUrl("http://localhost:8000");
        ocrProperties.setApiKey("secret-ocr-key");
        ocrProperties.setResponseTimeout(Duration.ofSeconds(600));

        ocrApiClient = new OcrApiClient(webClient, ocrProperties, objectMapper);
    }

    private void setupWebClientChain() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/api/v1/extract")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("Should successfully return OCR JSON response when API succeeds")
    void analyzeStatement_Success() {
        setupWebClientChain();
        String validJson = "{\"success\": true, \"request_id\": \"123\", \"statements\": {}}";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(validJson));

        String result = ocrApiClient.analyzeFinancialStatement(
                "dummy content".getBytes(), "report.pdf", "current", true, true
        );

        assertThat(result).isEqualTo(validJson);
    }

    @Test
    @DisplayName("Should throw OcrTimeoutException when 600s response timeout is exceeded")
    void analyzeStatement_Timeout_ThrowsOcrTimeoutException() {
        setupWebClientChain();
        ReadTimeoutException timeoutEx = ReadTimeoutException.INSTANCE;
        WebClientRequestException requestException = new WebClientRequestException(
                timeoutEx, HttpMethod.POST, URI.create("http://localhost:8000/api/v1/extract"), new HttpHeaders()
        );
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(requestException));

        assertThatThrownBy(() -> ocrApiClient.analyzeFinancialStatement(
                "dummy content".getBytes(), "report.pdf", "current", true, true
        ))
                .isInstanceOf(OcrTimeoutException.class)
                .hasMessage("OCR processing exceeded the configured timeout of 600 seconds.")
                .satisfies(ex -> assertThat(((OcrTimeoutException) ex).getErrorCode()).isEqualTo("OCR_TIMEOUT"));
    }

    @Test
    @DisplayName("Should throw OcrApiException with OCR_UNAUTHORIZED when OCR server returns 401")
    void analyzeStatement_401Unauthorized_ThrowsOcrApiException() {
        setupWebClientChain();
        WebClientResponseException responseEx = WebClientResponseException.create(
                HttpStatus.UNAUTHORIZED.value(), "Unauthorized", new HttpHeaders(), new byte[0], null
        );
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(responseEx));

        assertThatThrownBy(() -> ocrApiClient.analyzeFinancialStatement(
                "dummy content".getBytes(), "report.pdf", "current", true, true
        ))
                .isInstanceOf(OcrApiException.class)
                .satisfies(ex -> {
                    OcrApiException apiEx = (OcrApiException) ex;
                    assertThat(apiEx.getErrorCode()).isEqualTo("OCR_UNAUTHORIZED");
                    assertThat(apiEx.getStatusCode()).isEqualTo(401);
                });
    }

    @Test
    @DisplayName("Should throw OcrApiException with OCR_SERVER_BUSY when OCR server returns 503")
    void analyzeStatement_503ServerBusy_ThrowsOcrApiException() {
        setupWebClientChain();
        WebClientResponseException responseEx = WebClientResponseException.create(
                HttpStatus.SERVICE_UNAVAILABLE.value(), "Service Unavailable", new HttpHeaders(), new byte[0], null
        );
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(responseEx));

        assertThatThrownBy(() -> ocrApiClient.analyzeFinancialStatement(
                "dummy content".getBytes(), "report.pdf", "current", true, true
        ))
                .isInstanceOf(OcrApiException.class)
                .satisfies(ex -> {
                    OcrApiException apiEx = (OcrApiException) ex;
                    assertThat(apiEx.getErrorCode()).isEqualTo("OCR_SERVER_BUSY");
                    assertThat(apiEx.getStatusCode()).isEqualTo(503);
                });
    }

    @Test
    @DisplayName("Should throw OcrApiException with OCR_INVALID_RESPONSE when server returns non-JSON")
    void analyzeStatement_InvalidJson_ThrowsOcrApiException() {
        setupWebClientChain();
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("<html><body>502 Bad Gateway</body></html>"));

        assertThatThrownBy(() -> ocrApiClient.analyzeFinancialStatement(
                "dummy content".getBytes(), "report.pdf", "current", true, true
        ))
                .isInstanceOf(OcrApiException.class)
                .satisfies(ex -> {
                    OcrApiException apiEx = (OcrApiException) ex;
                    assertThat(apiEx.getErrorCode()).isEqualTo("OCR_INVALID_RESPONSE");
                });
    }
}
