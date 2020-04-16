package org.psc.playground.configuration;

import lombok.extern.slf4j.Slf4j;
import org.psc.playground.ConfigurationProvider;
import org.psc.playground.ConfiguredResult;
import org.psc.playground.MiscConfigurationBase;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;

@Slf4j
@Configuration
public class MiscConfiguration implements MiscConfigurationBase, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void configure(ConfigurationProvider configurationProvider) {
        configurationProvider.setDescription("TEST");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void applicationReady() {
        var configuredResult = applicationContext.getBean(ConfiguredResult.class);
        log.info("configuredResult.description = {}", configuredResult);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
