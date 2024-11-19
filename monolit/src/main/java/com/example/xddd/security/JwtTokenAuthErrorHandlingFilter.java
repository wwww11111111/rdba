package com.example.xddd.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static ch.qos.logback.core.util.AggregationType.NOT_FOUND;
import static org.springframework.http.HttpStatus.FORBIDDEN;

public class JwtTokenAuthErrorHandlingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    public JwtTokenAuthErrorHandlingFilter() {
        this.objectMapper = Jackson2ObjectMapperBuilder.json().build();
    }

    @Override
    protected void doFilterInternal(
            final @NonNull HttpServletRequest request,
            final @NonNull HttpServletResponse response,
            final @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (final JwtException e) {
            filterChain.doFilter(request, response);
            //токен плохой
//            HttpUtil.writeRestErrorResponse(request, response, e, FORBIDDEN, objectMapper);
        } catch (final UsernameNotFoundException e) {
//            HttpUtil.writeRestErrorResponse(request, response, e, NOT_FOUND, objectMapper);
        }
    }
}
