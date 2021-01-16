package org.psc.playground;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@SpringBootApplication
public class PlaygroundWsWorkerApplication {

    @Value("${greeting-message}")
    private String greeting;

    public static void main(String[] args) {
        SpringApplication.run(PlaygroundWsWorkerApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void applicationStarted(){
        log.info("{}", greeting);
    }

}
