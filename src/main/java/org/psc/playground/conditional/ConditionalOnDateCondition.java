package org.psc.playground.conditional;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ConditionalOnDateCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean matches = true;

        LocalDate current = LocalDate.now();
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(ConditionalOnDate.class.getName());

        if (annotationAttributes != null && !annotationAttributes.isEmpty()) {
            String dateString = (String) annotationAttributes.getOrDefault("value", "");
            LocalDate condition = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
            matches = current.equals(condition);
        }

        return matches;
    }
}
