package org.xresource.demo.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Allow all origins
        corsConfiguration.setAllowedOriginPatterns(Collections.singletonList("*"));

        // Allow all methods (GET, POST, PUT, DELETE, etc.)
        corsConfiguration.addAllowedMethod("*");

        // Allow all headers
        corsConfiguration.addAllowedHeader("*");

        // Enable credentials (cookies, authorization headers)
        corsConfiguration.setAllowCredentials(true);

        // Apply the CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }
}
