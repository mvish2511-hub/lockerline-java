package com.lockerline.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Every request also leaves one log line on stdout - method, path, status and id; never a body, so never a code.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String KEY = "requestId";

    private static final Logger log = LoggerFactory.getLogger("com.lockerline.request");

    @Override
    protected void doFilterInternal(           HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
                String id = UUID.randomUUID().toString().substring(0, 12);
        MDC.put(KEY, id);
        try {
            chain.doFilter(request, response);
        } finally {
                    log.info("request method={} path={} status={} requestId={}",     request.getMethod(), request.getRequestURI(),
                    response.getStatus(), id);
            MDC.remove(KEY);
        }
    }
}
