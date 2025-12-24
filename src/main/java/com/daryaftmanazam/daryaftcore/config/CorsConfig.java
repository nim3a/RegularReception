package com.daryaftmanazam.daryaftcore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) configuration.
 * Allows frontend applications to access the REST API.
 */
@Configuration
public class CorsConfig {

    /**
     * Configure CORS to allow frontend access.
     * 
     * @return CorsFilter with configured settings
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow credentials (cookies, authorization headers, etc.)
        config.setAllowCredentials(true);
        
        // Allow requests from these origins (customize for production)
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://*.example.com" // Replace with your production domain
        ));
        
        // Allow these HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Allow these headers
        config.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        
        // Expose these headers to the frontend
        config.setExposedHeaders(Arrays.asList(
                "Content-Type",
                "X-Total-Count",
                "X-Total-Pages",
                "Authorization"
        ));
        
        // Cache preflight requests for 1 hour
        config.setMaxAge(3600L);
        
        // Apply this configuration to all endpoints
        source.registerCorsConfiguration("/api/**", config);
        
        return new CorsFilter(source);
    }
}
