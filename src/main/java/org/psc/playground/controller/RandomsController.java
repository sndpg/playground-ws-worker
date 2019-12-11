package org.psc.playground.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;

@Slf4j
@Controller
public class RandomsController implements InitializingBean {

    private SecureRandom secureRandom;

    @MessageMapping("randomDecimals")
    public Flux<BigDecimal> randomBigDecimal() {
        return Flux.fromStream(secureRandom.doubles(0, 100).mapToObj(BigDecimal::valueOf))
                .doOnEach(signal -> log.info(Objects.requireNonNull(signal.get()).toPlainString()))
                .delayElements(Duration.ofMillis(250));
    }

    @Override
    public void afterPropertiesSet() {
        secureRandom = new SecureRandom();
    }
}
