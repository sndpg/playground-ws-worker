package org.psc.playground.configuration;

import io.woof.database.QueryResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.core.publisher.ReplayProcessor;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Slf4j
@Configuration
@EnableSwagger2
public class ApplicationConfiguration implements WebMvcConfigurer {

    @Bean
    public QueryResolver queryResolver() {
        QueryResolver resolvedQueries = QueryResolver.builder()
                .resource("queries")
                .build();

        resolvedQueries.getStatement("queries/selectOne").ifPresent(statement -> log.info("selectOne: {}", statement));
        resolvedQueries.getStatement("queries/test/selectAnotherOne")
                .ifPresent(statement -> log.info("selectAnotherOne: {}", statement));

        return resolvedQueries;
    }

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("playground")
                .apiInfo(new ApiInfoBuilder().description("playground").version("1.0").build())
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build()
                .enableUrlTemplating(false);
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //        registry.addResourceHandler("/webjars/**")
        //                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        //        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
        //        registry.addResourceHandler("/swagger-ui.html").addResourceLocations
        //        ("classpath:/META-INF/resources/");
    }

    @Bean
    public ReplayProcessor<String> echoParameters() {
        return ReplayProcessor.create(100);
    }

}
