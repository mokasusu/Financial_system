package com.financial.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class OcrWebClientConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(OcrProperties.class, OcrWebClientConfig.class);

    @Test
    @DisplayName("Should create ocrWebClient bean with default timeout and base URL")
    void ocrWebClientBeanCreation_DefaultProperties() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("ocrWebClient");
            WebClient webClient = (WebClient) context.getBean("ocrWebClient");
            assertThat(webClient).isNotNull();
        });
    }

    @Test
    @DisplayName("Should create ocrWebClient bean with custom properties from environment")
    void ocrWebClientBeanCreation_CustomProperties() {
        contextRunner
                .withPropertyValues(
                        "app.ocr.base-url=http://ocr-service:8080",
                        "app.ocr.api-key=test-api-key-999",
                        "app.ocr.connect-timeout=15000",
                        "app.ocr.response-timeout=600s"
                )
                .run(context -> {
                    assertThat(context).hasBean("ocrWebClient");
                    WebClient webClient = (WebClient) context.getBean("ocrWebClient");
                    assertThat(webClient).isNotNull();
                });
    }
}

