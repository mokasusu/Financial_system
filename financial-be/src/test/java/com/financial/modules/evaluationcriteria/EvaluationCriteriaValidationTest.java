package com.financial.modules.evaluationcriteria;

import com.financial.modules.evaluationcriteria.entity.EvaluationCriteria;
import com.financial.modules.evaluationcriteria.service.EvaluationCriteriaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluationCriteriaValidationTest {

    @Test
    @DisplayName("Evaluation Criteria module must NOT contain calculation engine or scoring evaluators")
    void shouldNotContainCalculationEngineClasses() {
        String basePkg = "com.financial.modules.evaluationcriteria";

        List<String> prohibitedClassNames = List.of(
                "EvaluationEngine",
                "ScoringEngine",
                "RatioCalculationEngine",
                "RiskAnalysisEngine",
                "AiEvaluationService",
                "OpenRouterClient"
        );

        for (String prohibited : prohibitedClassNames) {
            try {
                Class.forName(basePkg + ".service." + prohibited);
                org.junit.jupiter.api.Assertions.fail("Prohibited calculation or AI class found in Evaluation Criteria module: " + prohibited);
            } catch (ClassNotFoundException ignored) {
                // Expected
            }
        }
    }

    @Test
    @DisplayName("Service methods must only manage criteria definitions (no ratio calculation or scoring methods)")
    void serviceShouldOnlyManageCriteria() {
        Method[] methods = EvaluationCriteriaService.class.getDeclaredMethods();

        List<String> prohibitedMethodPrefixes = List.of(
                "calculate", "score", "evaluateFinancial", "analyzeRisk", "generateAi"
        );

        for (Method method : methods) {
            for (String prefix : prohibitedMethodPrefixes) {
                assertThat(method.getName().toLowerCase())
                        .withFailMessage("Service method '%s' suggests calculation or AI logic which is prohibited in this module", method.getName())
                        .doesNotStartWith(prefix.toLowerCase());
            }
        }
    }
}

