package org.psc.playground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class PlaygroundWsWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlaygroundWsWorkerApplication.class, args);
    }

}
