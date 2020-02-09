package org.psc.playground;

import org.junit.jupiter.api.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SpringExpressionLanguageTest {

    @Test
    void testExpressionResolution(){
        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext evaluationContext = SimpleEvaluationContext.forReadWriteDataBinding().build();

        MultiValueMap<String, Object> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("numericValue", 1234);

        evaluationContext.setVariable("requestParams", requestParams);
        requestParams.forEach(evaluationContext::setVariable);

        String resolvable = "{ numericValue: #requestParams['numericValue'] }";

        Object resolved = parser.parseExpression(resolvable).getValue(evaluationContext);

        assertThat(resolved, is(not(nullValue())));
    }
}
