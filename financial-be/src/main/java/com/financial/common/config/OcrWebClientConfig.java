package com.financial.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(OcrProperties.class)
public class OcrWebClientConfig {

    public static final String OCR_WEB_CLIENT_BEAN = "ocrWebClient";
    private static final int MAX_IN_MEMORY_BUFFER_SIZE = 50 * 1024 * 1024; // 50MB for large scanned financial files

    private final OcrProperties ocrProperties;

    public OcrWebClientConfig(OcrProperties ocrProperties) {
        this.ocrProperties = ocrProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    /**
     * Dedicated WebClient bean configured specifically for the external OCR API.
     * Applies configured connection timeout, 600-second default response timeout,
     * base URL, X-API-Key header, and multipart file buffer support.
     *
     * Will NOT affect other HTTP clients or WebClient instances in the application.
     */
    @Bean
    @Qualifier(OCR_WEB_CLIENT_BEAN)
    public WebClient ocrWebClient() {
        Duration connectTimeout = ocrProperties.getConnectTimeout() != null
                ? ocrProperties.getConnectTimeout()
                : Duration.ofMillis(10000);

        Duration responseTimeout = ocrProperties.getResponseTimeout() != null
                ? ocrProperties.getResponseTimeout()
                : Duration.ofSeconds(600);

        long responseTimeoutSeconds = Math.max(1, responseTimeout.toSeconds());

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis())
                .responseTimeout(responseTimeout)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(responseTimeoutSeconds, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(responseTimeoutSeconds, TimeUnit.SECONDS))
                );

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_BUFFER_SIZE))
                .build();

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(ocrProperties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies);

        if (ocrProperties.getApiKey() != null && !ocrProperties.getApiKey().isBlank()) {
            builder.defaultHeader("X-API-Key", ocrProperties.getApiKey());
        }

        return builder.build();
    }
}

