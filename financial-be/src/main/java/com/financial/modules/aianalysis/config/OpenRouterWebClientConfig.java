package com.financial.modules.aianalysis.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(OpenRouterProperties.class)
public class OpenRouterWebClientConfig {

    public static final String OPENROUTER_WEB_CLIENT_BEAN = "openRouterWebClient";
    private static final int MAX_IN_MEMORY_BUFFER_SIZE = 10 * 1024 * 1024; // 10MB

    private final OpenRouterProperties properties;

    public OpenRouterWebClientConfig(OpenRouterProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Qualifier(OPENROUTER_WEB_CLIENT_BEAN)
    public WebClient openRouterWebClient() {
        Duration timeout = properties.getTimeout() != null
                ? properties.getTimeout()
                : Duration.ofSeconds(120);

        long timeoutSeconds = Math.max(1, timeout.toSeconds());

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000)
                .responseTimeout(timeout)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS)));

        String baseUrl = (properties.getBaseUrl() != null && !properties.getBaseUrl().isBlank())
                ? properties.getBaseUrl().trim()
                : "https://openrouter.ai/api/v1";

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("HTTP-Referer", "https://financial.com")
                .defaultHeader("X-Title", "Enterprise Financial Health Assessment")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_BUFFER_SIZE));

        return builder.build();
    }
}

