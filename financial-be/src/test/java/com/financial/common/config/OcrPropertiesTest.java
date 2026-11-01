package com.financial.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class OcrPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(OcrProperties.class);

    @Test
    @DisplayName("Should initialize with sensible default values (10s connect, 600s response timeout)")
    void defaultValues() {
        OcrProperties properties = new OcrProperties();

        assertThat(properties.getBaseUrl()).isEqualTo("http://localhost:8000");
        assertThat(properties.getApiKey()).isEmpty();
        assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofMillis(10000));
        assertThat(properties.getResponseTimeout()).isEqualTo(Duration.ofSeconds(600));
        assertThat(properties.getResponseTimeout().toSeconds()).isEqualTo(600L);
    }

    @Test
    @DisplayName("Should bind configuration properties from application.properties or environment")
    void customPropertiesBinding() {
        contextRunner
                .withPropertyValues(
                        "app.ocr.base-url=https://ocr.example.com",
                        "app.ocr.api-key=secret-token-xyz",
                        "app.ocr.connect-timeout=5000",
                        "app.ocr.response-timeout=300s"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(OcrProperties.class);
                    OcrProperties properties = context.getBean(OcrProperties.class);

                    assertThat(properties.getBaseUrl()).isEqualTo("https://ocr.example.com");
                    assertThat(properties.getApiKey()).isEqualTo("secret-token-xyz");
                    assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofMillis(5000));
                    assertThat(properties.getResponseTimeout()).isEqualTo(Duration.ofSeconds(300));
                });
    }

    @Test
    @DisplayName("Should support changing response timeout via OCR_RESPONSE_TIMEOUT_SECONDS without code changes")
    void responseTimeoutOverride() {
        contextRunner
                .withPropertyValues("app.ocr.response-timeout=1200s")
                .run(context -> {
                    OcrProperties properties = context.getBean(OcrProperties.class);
                    assertThat(properties.getResponseTimeout().toSeconds()).isEqualTo(1200L);
                });
    }
}

