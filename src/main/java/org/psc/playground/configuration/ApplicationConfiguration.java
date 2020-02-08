package org.psc.playground.configuration;

import org.psc.playground.security.JwtAuthenticationFilter;
import org.psc.playground.security.OncePerRequestJwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
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

@Configuration
@EnableSwagger2
@EnableWebSecurity
public class ApplicationConfiguration extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {
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

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        //        httpSecurity.addFilterBefore(jwtAuthenticationFilter(null), UsernamePasswordAuthenticationFilter
        //        .class);
        httpSecurity.httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .anonymous(httpSecurityAnonymousConfigurer -> httpSecurityAnonymousConfigurer.authenticationProvider(
                        authenticationProvider()))
                .userDetailsService(
                        username -> User.builder().password("").username("test").authorities("USER").build())
                .authenticationProvider(authenticationProvider())
                .sessionManagement(AbstractHttpConfigurer::disable)
                .authorizeRequests(authorizeRequest -> authorizeRequest.anyRequest().permitAll());
        //        httpSecurity.addFilterBefore(oncePerRequestJwtAuthenticationFilter(),
        //        UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                return new UsernamePasswordAuthenticationToken(
                        User.builder().password("").username("test").authorities("USER").build(), "",
                        AuthorityUtils.createAuthorityList("USER"));
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return true;
            }
        };
    }

    //        @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager);
        return jwtAuthenticationFilter;
    }

    //        @Bean
    //    @ConditionalOnBean(JwtAuthenticationFilter.class)
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterFilterRegistrationBean(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        FilterRegistrationBean<JwtAuthenticationFilter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(jwtAuthenticationFilter);
        filterFilterRegistrationBean.setEnabled(false);
        return filterFilterRegistrationBean;
    }

    //    @Bean
    public OncePerRequestJwtAuthenticationFilter oncePerRequestJwtAuthenticationFilter() {
        OncePerRequestJwtAuthenticationFilter jwtAuthenticationFilter = new OncePerRequestJwtAuthenticationFilter();
        return jwtAuthenticationFilter;
    }

    //    @Bean
    //    @ConditionalOnBean(OncePerRequestJwtAuthenticationFilter.class)
    public FilterRegistrationBean<OncePerRequestJwtAuthenticationFilter> oncePerRequestJwtAuthenticationFilterFilterRegistrationBean(
            OncePerRequestJwtAuthenticationFilter jwtAuthenticationFilter) {
        FilterRegistrationBean<OncePerRequestJwtAuthenticationFilter> filterFilterRegistrationBean =
                new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(jwtAuthenticationFilter);
        filterFilterRegistrationBean.setEnabled(false);
        return filterFilterRegistrationBean;
    }

    @Bean
    public ReplayProcessor<String> echoParameters() {
        return ReplayProcessor.create(100);
    }

}
