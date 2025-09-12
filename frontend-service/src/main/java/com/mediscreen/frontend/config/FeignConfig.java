package com.mediscreen.frontend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Feign pour Basic Auth automatique
 * 
 * Configure l'intercepteur Basic Auth pour sécuriser 
 * les communications inter-services.
 * 
 * Note: @EnableFeignClients est déjà dans FrontendServiceApplication
 */
@Configuration
public class FeignConfig {
    
    @Bean
    public BasicAuthFeignInterceptor basicAuthFeignInterceptor() {
        return new BasicAuthFeignInterceptor();
    }
}