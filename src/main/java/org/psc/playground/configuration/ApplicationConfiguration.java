package org.psc.playground.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.core.publisher.ReplayProcessor;

@Slf4j
@Configuration
public class ApplicationConfiguration implements WebMvcConfigurer {

//    @Bean
//    public QueryResolver queryResolver() {
//        QueryResolver resolvedQueries = QueryResolver.builder()
//                .resource("queries")
//                .build();
//
//        resolvedQueries.getStatement("queries/selectOne").ifPresent(statement -> log.info("selectOne: {}", statement));
//        resolvedQueries.getStatement("queries/test/selectAnotherOne")
//                .ifPresent(statement -> log.info("selectAnotherOne: {}", statement));
//
//        return resolvedQueries;
//    }


    @Bean
    public ReplayProcessor<String> echoParameters() {
        return ReplayProcessor.create(100);
    }

}
