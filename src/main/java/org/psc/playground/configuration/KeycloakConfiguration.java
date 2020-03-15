package org.psc.playground.configuration;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Configuration} for KeyCloak-Integration
 */
@Configuration
public class KeycloakConfiguration {

    /**
     * Bug?: https://stackoverflow.com/questions/57787768/issues-running-example-keycloak-spring-boot-app
     *
     * @return KeycloakSpringBootConfigResolver
     */
    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

}
