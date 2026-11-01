package com.financial.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.ocr")
public class OcrProperties {

    /**
     * Base URL for the external OCR service. Defaults to http://localhost:8000
     */
    private String baseUrl = "http://localhost:8000";

    /**
     * API Key for authenticating with the external OCR service (sent via X-API-Key header).
     */
    private String apiKey = "";

    /**
     * Connection timeout. Defaults to 10,000 milliseconds (10 seconds).
     */
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration connectTimeout = Duration.ofMillis(10000);

    /**
     * Response timeout. Defaults to 600 seconds (10 minutes).
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration responseTimeout = Duration.ofSeconds(600);

    /**
     * Maximum file upload size in megabytes. Defaults to 25 MB.
     */
    private int maxUploadMb = 25;
}
