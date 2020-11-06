package org.psc.playground.filter;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@ConditionalOnProperty("application.filter.redirect-filter.enable")
@Slf4j
public class RedirectFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            @NotNull FilterChain filterChain) {
        log.info(request.getServletPath());
        log.info(request.getRequestURL().toString());
        request.getRequestDispatcher("/api" + request.getRequestURI());
    }
}
