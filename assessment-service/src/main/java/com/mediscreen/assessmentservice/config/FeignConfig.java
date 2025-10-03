package com.mediscreen.assessmentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Feign pour Basic Auth automatique
 *
 * Configure l'intercepteur Basic Auth pour sécuriser
 * les communications inter-services via Gateway.
 *
 * Note: @EnableFeignClients est déjà dans AssessmentServiceApplication
 */
@Configuration
public class FeignConfig {

    @Bean
    public BasicAuthFeignInterceptor basicAuthFeignInterceptor() {
        return new BasicAuthFeignInterceptor();
    }
}