package org.psc.playground.conditional;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(ConditionalOnDateCondition.class)
public @interface ConditionalOnDate {
    String value();
}
