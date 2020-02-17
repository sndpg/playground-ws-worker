package org.psc.playground.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "application.security.enable-method-security", havingValue = "true", matchIfMissing =
        true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class PlaygroundGlobalMethodSecurity extends GlobalMethodSecurityConfiguration {

    @PostConstruct
    public void postConstruct(){
        log.info("GlobalMethodSecurity enabled");
    }

}
