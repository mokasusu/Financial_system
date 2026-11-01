package com.financial.modules.aianalysis.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.ai.openrouter")
public class OpenRouterProperties {

    private String baseUrl = "https://openrouter.ai/api/v1";
    private String apiKey = "";
    private String model = "openrouter/free";
    private Duration timeout = Duration.ofSeconds(120);

    public void setTimeout(String timeout) {
        if (timeout == null || timeout.isBlank()) {
            this.timeout = Duration.ofSeconds(120);
            return;
        }
        String cleaned = timeout.trim().toLowerCase();
        if (cleaned.endsWith("s")) {
            long seconds = Long.parseLong(cleaned.substring(0, cleaned.length() - 1));
            this.timeout = Duration.ofSeconds(seconds);
        } else if (cleaned.endsWith("ms")) {
            long ms = Long.parseLong(cleaned.substring(0, cleaned.length() - 2));
            this.timeout = Duration.ofMillis(ms);
        } else {
            this.timeout = Duration.ofSeconds(Long.parseLong(cleaned));
        }
    }
}

