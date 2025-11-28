package com.proj.backend.config;

import com.proj.backend.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

    private final JwtCore jwtCore;
    private final UserDetailsService userDetailsService; // Spring найдет твой UserService, т.к. он реализует UserDetailsService?
    // А, стоп. Твой UserService НЕ реализует UserDetailsService напрямую, но внутри User есть имплементация.
    // Нам нужно немного доработать UserService (см. Шаг 4), пока оставь так.

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = null;
        String email = null;
        String headerAuth = request.getHeader("Authorization");

        // Проверяем заголовок: "Bearer <token>"
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            jwt = headerAuth.substring(7);
        }

        // Если токен есть и он валиден
        if (jwt != null) {
            try {
                if (jwtCore.validateToken(jwt)) {
                    email = jwtCore.getNameFromJwt(jwt);
                }

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                // Логируем ошибку, но не валим приложение
                System.out.println("Ошибка аутентификации JWT: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}