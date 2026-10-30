package com.lockerline.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Every request gets its own id, kept in the logging context (MDC) while the request runs, so an unexpected
 * error can name it in the answer and in the log line - and the two can be matched up.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String KEY = "requestId";

    @Override
    protected void doFilterInternal(           HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        MDC.put(KEY, UUID.randomUUID().toString().substring(0, 12));
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(KEY);
        }
    }
}
