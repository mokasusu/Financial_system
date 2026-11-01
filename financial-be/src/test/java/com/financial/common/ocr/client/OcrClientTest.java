package com.financial.common.ocr.client;

import com.financial.common.config.OcrProperties;
import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.OcrTimeoutException;
import com.financial.common.response.ApiResponse;
import io.netty.channel.ConnectTimeoutException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class OcrClientTest {

    @Mock
    private WebClient webClient;

    private OcrProperties ocrProperties;
    private OcrClient ocrClient;

    @BeforeEach
    void setUp() {
        ocrProperties = new OcrProperties();
        ocrProperties.setBaseUrl("http://localhost:8000");
        ocrProperties.setApiKey("sensitive-ocr-api-key-12345");
        ocrProperties.setConnectTimeout(Duration.ofMillis(10000));
        ocrProperties.setResponseTimeout(Duration.ofSeconds(600));

        ocrClient = new OcrClient(webClient, ocrProperties);
    }

    @Test
    @DisplayName("Should detect java.util.concurrent.TimeoutException and throw OcrTimeoutException with 600s")
    void mapTimeoutException_ConcurrentTimeoutException() {
        TimeoutException timeoutException = new TimeoutException("Channel timed out");

        RuntimeException result = ocrClient.mapTimeoutException(timeoutException);

        assertThat(result).isInstanceOf(OcrTimeoutException.class);
        OcrTimeoutException ocrEx = (OcrTimeoutException) result;
        assertThat(ocrEx.getErrorCode()).isEqualTo("OCR_TIMEOUT");
        assertThat(ocrEx.getMessage()).isEqualTo("OCR processing exceeded the configured timeout of 600 seconds.");
        assertThat(ocrEx.getTimeoutSeconds()).isEqualTo(600L);
    }

    @Test
    @DisplayName("Should detect Netty ReadTimeoutException and throw OcrTimeoutException")
    void mapTimeoutException_NettyReadTimeoutException() {
        ReadTimeoutException readTimeoutException = ReadTimeoutException.INSTANCE;

        RuntimeException result = ocrClient.mapTimeoutException(readTimeoutException);

        assertThat(result).isInstanceOf(OcrTimeoutException.class);
        OcrTimeoutException ocrEx = (OcrTimeoutException) result;
        assertThat(ocrEx.getErrorCode()).isEqualTo("OCR_TIMEOUT");
        assertThat(ocrEx.getMessage()).isEqualTo("OCR processing exceeded the configured timeout of 600 seconds.");
    }

    @Test
    @DisplayName("Should detect Netty ConnectTimeoutException and throw OcrTimeoutException")
    void mapTimeoutException_ConnectTimeoutException() {
        ConnectTimeoutException connectTimeoutException = new ConnectTimeoutException("connection timed out: localhost/127.0.0.1:8000");

        RuntimeException result = ocrClient.mapTimeoutException(connectTimeoutException);

        assertThat(result).isInstanceOf(OcrTimeoutException.class);
        OcrTimeoutException ocrEx = (OcrTimeoutException) result;
        assertThat(ocrEx.getErrorCode()).isEqualTo("OCR_TIMEOUT");
    }

    @Test
    @DisplayName("Should detect nested timeout inside WebClientRequestException")
    void mapTimeoutException_WrappedInWebClientRequestException() {
        ReadTimeoutException cause = ReadTimeoutException.INSTANCE;
        WebClientRequestException requestException = new WebClientRequestException(
                cause, HttpMethod.POST, URI.create("http://localhost:8000/api/ocr/process"), new HttpHeaders()
        );

        RuntimeException result = ocrClient.mapTimeoutException(requestException);

        assertThat(result).isInstanceOf(OcrTimeoutException.class);
        OcrTimeoutException ocrEx = (OcrTimeoutException) result;
        assertThat(ocrEx.getErrorCode()).isEqualTo("OCR_TIMEOUT");
        assertThat(ocrEx.getMessage()).isEqualTo("OCR processing exceeded the configured timeout of 600 seconds.");
    }

    @Test
    @DisplayName("Should dynamically format timeout message when response timeout is configured to custom seconds")
    void mapTimeoutException_CustomConfiguredTimeout() {
        ocrProperties.setResponseTimeout(Duration.ofSeconds(300)); // 5 minutes

        TimeoutException timeoutException = new TimeoutException("Deadline exceeded");
        RuntimeException result = ocrClient.mapTimeoutException(timeoutException);

        assertThat(result).isInstanceOf(OcrTimeoutException.class);
        OcrTimeoutException ocrEx = (OcrTimeoutException) result;
        assertThat(ocrEx.getErrorCode()).isEqualTo("OCR_TIMEOUT");
        assertThat(ocrEx.getMessage()).isEqualTo("OCR processing exceeded the configured timeout of 300 seconds.");
        assertThat(ocrEx.getTimeoutSeconds()).isEqualTo(300L);
    }

    @Test
    @DisplayName("Should never expose API key or sensitive headers in timeout exception message")
    void mapTimeoutException_DoesNotExposeApiKey() {
        TimeoutException timeoutException = new TimeoutException("Timeout occurred with apiKey=sensitive-ocr-api-key-12345");
        RuntimeException result = ocrClient.mapTimeoutException(timeoutException);

        assertThat(result.getMessage()).doesNotContain("sensitive-ocr-api-key-12345");
        assertThat(result.getMessage()).isEqualTo("OCR processing exceeded the configured timeout of 600 seconds.");
    }

    @Test
    @DisplayName("GlobalExceptionHandler should map OcrTimeoutException to HTTP 504 with OCR_TIMEOUT code")
    void globalExceptionHandler_HandlesOcrTimeoutException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        OcrTimeoutException exception = new OcrTimeoutException(600L);

        ResponseEntity<ApiResponse<Void>> response = handler.handleOcrTimeoutException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("OCR_TIMEOUT");
        assertThat(response.getBody().getMessage()).isEqualTo("OCR processing exceeded the configured timeout of 600 seconds.");
    }
}

