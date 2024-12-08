package com.arangodb.spring.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class DynamicCorsFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, jakarta.servlet.FilterChain filterChain) throws jakarta.servlet.ServletException, IOException {

        String origin = ((HttpServletRequest) request).getHeader("Origin");

        if (origin != null && origin.startsWith("chrome-extension://")) {
            // Si l'origine est une extension Chrome, on l'accepte dynamiquement
            response.setHeader("Access-Control-Allow-Origin", origin);
        }

        // Passer la requête au prochain filtre
        filterChain.doFilter(request, response);
    }
}
