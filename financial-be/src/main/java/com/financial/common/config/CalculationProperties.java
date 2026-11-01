package com.financial.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.RoundingMode;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.calculation")
public class CalculationProperties {

    /**
     * Default decimal scale for ratio calculation results (default 4).
     */
    private int defaultScale = 4;

    /**
     * Default rounding mode applied to ratio calculation results (default HALF_UP).
     */
    private RoundingMode roundingMode = RoundingMode.HALF_UP;
}

