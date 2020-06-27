package org.psc.playground.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@ConditionalOnProperty("application.filter.misc-filter.enable")
@Slf4j
public class MiscFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        log.info(request.getServletPath());
        log.info(request.getRequestURL().toString());
        request.getRequestDispatcher("/api" + request.getRequestURI());
    }
}
