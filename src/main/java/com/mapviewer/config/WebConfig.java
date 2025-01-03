package com.mapviewer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Autoriser les endpoints commençant par /api/
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Méthodes autorisées
                        .allowedHeaders("Content-Type", "Authorization", "*")
                        .allowCredentials(true) // Permettre l'envoi de cookies
                        .allowedOrigins(getAllowedOrigins())
                        .maxAge(3600); // Cache les résultats de CORS pendant 1 heure
            }
        };
    }

    private String[] getAllowedOrigins() {
        // Dynamically return allowed origins, including the origin for Chrome extensions
        return new String[]{
                "https://localhost:4200",
                "https://mapviewer-72be0.web.app/", // Ajoutez votre frontend
                //"chrome-extension://gjpgfhjgdajmdhialdclpkjmfkhbdchl" // Autoriser toutes les extensions Chrome (moins sécurisé)
        };
    }

}
