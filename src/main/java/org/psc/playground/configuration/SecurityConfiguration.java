package org.psc.playground.configuration;

import org.psc.playground.security.JwtAuthenticationFilter;
import org.psc.playground.security.OncePerRequestJwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Configuration
@EnableWebSecurity
@Order(200)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.httpBasic(httpSecurityHttpBasicConfigurer -> httpSecurityHttpBasicConfigurer.init(httpSecurity))
                .csrf(AbstractHttpConfigurer::disable)
                .anonymous(httpSecurityAnonymousConfigurer -> httpSecurityAnonymousConfigurer.authenticationProvider(
                        authenticationProvider()))
                .authenticationProvider(authenticationProvider())
                .sessionManagement(AbstractHttpConfigurer::disable)
                .authorizeRequests(authorizeRequest -> authorizeRequest.anyRequest().permitAll());
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                List<GrantedAuthority> grantedAuthorities;

                String principal = (String) authentication.getPrincipal();
                if (principal.equals("admin")) {
                    grantedAuthorities = AuthorityUtils.createAuthorityList("USER", "ADMIN");
                } else if (principal.equals("user")) {
                    grantedAuthorities = AuthorityUtils.createAuthorityList("USER");
                } else {
                    grantedAuthorities = AuthorityUtils.createAuthorityList("NONE");
                }

                return new UsernamePasswordAuthenticationToken(
                        User.builder().password("").username("test").authorities(grantedAuthorities).build(),
                        authentication.getCredentials(), grantedAuthorities);
            }

            @Override
            public boolean supports(Class<?> authenticationClass) {
                return authenticationClass.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
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
}
