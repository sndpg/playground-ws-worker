package org.psc.playground;

import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AntPathMatcherTest {

    @Test
    void testMatch(){
        AntPathMatcher antPathMatcher = new AntPathMatcher();

        boolean match = antPathMatcher.match("/**/swagger-ui/**/**/*", "/swagger-ui/index.html");
        assertTrue(match);
    }
}
