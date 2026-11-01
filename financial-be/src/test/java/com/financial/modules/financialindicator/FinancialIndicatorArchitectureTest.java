package com.financial.modules.financialindicator;

import com.financial.modules.financialindicator.entity.FinancialIndicator;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FinancialIndicatorArchitectureTest {

    @Test
    @DisplayName("FinancialRatioDefinition must NOT store evaluation thresholds (thresholds belong to Evaluation Criteria module)")
    void shouldNotStoreEvaluationThresholdsInRatioDefinition() {
        List<String> prohibitedTerms = List.of(
                "threshold", "goodthreshold", "badthreshold", "minthreshold", "maxthreshold",
                "benchmark", "targetvalue", "warningthreshold", "criticalthreshold", "scoring", "evaluationscore"
        );

        Field[] fields = FinancialRatioDefinition.class.getDeclaredFields();
        for (Field field : fields) {
            String fieldNameLower = field.getName().toLowerCase();
            for (String prohibited : prohibitedTerms) {
                assertThat(fieldNameLower)
                        .withFailMessage("FinancialRatioDefinition contains prohibited threshold/evaluation field: " + field.getName())
                        .doesNotContain(prohibited);
            }
        }
    }

    @Test
    @DisplayName("FinancialIndicator must NOT store evaluation criteria or calculation state")
    void shouldNotStoreCalculationOrThresholdsInFinancialIndicator() {
        List<String> prohibitedTerms = List.of(
                "threshold", "scoring", "weight", "calculatedvalue", "rating"
        );

        Field[] fields = FinancialIndicator.class.getDeclaredFields();
        for (Field field : fields) {
            String fieldNameLower = field.getName().toLowerCase();
            for (String prohibited : prohibitedTerms) {
                assertThat(fieldNameLower)
                        .withFailMessage("FinancialIndicator contains prohibited field: " + field.getName())
                        .doesNotContain(prohibited);
            }
        }
    }

    @Test
    @DisplayName("Module package must NOT contain calculation engine or expression evaluators")
    void shouldNotContainCalculationEngineClasses() {
        Package pkg = FinancialIndicator.class.getPackage();
        String basePkg = "com.financial.modules.financialindicator";

        // Prohibited class names or methods executing arbitrary calculation
        List<String> prohibitedClassNames = List.of(
                "FinancialCalculationEngine",
                "RatioCalculationEngine",
                "ExpressionEvaluator",
                "ScriptEngineExecutor"
        );

        for (String prohibited : prohibitedClassNames) {
            try {
                Class.forName(basePkg + ".service." + prohibited);
                org.junit.jupiter.api.Assertions.fail("Prohibited calculation engine class found: " + prohibited);
            } catch (ClassNotFoundException ignored) {
                // Expected: class should not exist
            }
        }
    }

    @Test
    @DisplayName("Service methods must only be read/query operations (no calculate methods)")
    void serviceShouldNotExposeCalculationMethods() {
        Class<?> serviceClass = com.financial.modules.financialindicator.service.FinancialIndicatorService.class;
        Method[] methods = serviceClass.getDeclaredMethods();

        List<String> prohibitedMethodPrefixes = List.of("calculate", "evaluate", "score", "executeFormula");

        for (Method method : methods) {
            for (String prefix : prohibitedMethodPrefixes) {
                assertThat(method.getName().toLowerCase())
                        .withFailMessage("Service method '%s' suggests calculation logic which is outside module scope", method.getName())
                        .doesNotStartWith(prefix.toLowerCase());
            }
        }
    }
}
