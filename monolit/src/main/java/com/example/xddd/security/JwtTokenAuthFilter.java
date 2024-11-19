package com.example.xddd.security;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtTokenAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final @org.springframework.lang.NonNull HttpServletResponse response,
            final @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_TOKEN_PREFIX)) {
//            log.debug("Caught unauthorized request for URL={}", request.getRequestURL().toString());
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtToken = authHeader.replace(BEARER_TOKEN_PREFIX, "");
        try {
            if (!jwtUtil.isTokenExpired(jwtToken)) {
                final var username = jwtUtil.getUsername(jwtToken);
                final var user = userDetailsService.loadUserByUsername(username);

                final Authentication authentication =
                        new UsernamePasswordAuthenticationToken(user, null, jwtUtil.getAuthorities(jwtToken));
                SecurityContextHolder.getContext().setAuthentication(authentication);
//                System.out.println("засетили аутентификацию");
            }
        } catch (final JwtException e) {
//            log.warn("Error handling JWT token: {}", e.getMessage());
            throw e;
        } catch (final UsernameNotFoundException e) {
//            log.warn("Username was not found during JWT authentication");
            throw e;
        }

        filterChain.doFilter(request, response);
    }
}
