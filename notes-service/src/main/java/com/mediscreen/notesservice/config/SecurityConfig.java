package com.mediscreen.notesservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration de sécurité pour le microservice Notes
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Value("${mediscreen.auth.username}")
    private String username;

    @Value("${mediscreen.auth.password}")
    private String password;

    /**
     * Configuration de la chaîne de filtres de sécurité
     * Validation Basic Auth pour empêcher l'accès direct au service
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // Désactive CSRF pour l'API REST (stateless)
            .csrf(AbstractHttpConfigurer::disable)

            // Configuration CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Gestion des sessions (stateless pour microservice)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Configuration des autorisations - Basic Auth requis
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                    .anyRequest().authenticated()
            )
            
            // Activation de l'authentification HTTP Basic
            .httpBasic(basic -> {})

            // Configuration des headers de sécurité
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentType -> {})
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
            )

            .build();
    }
    
    /**
     * Configuration des utilisateurs autorisés pour Basic Auth
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails serviceUser = User.builder()
                .username(username)
                .password(passwordEncoder().encode(password))
                .roles("SERVICE")
                .build();
        
        return new InMemoryUserDetailsManager(serviceUser);
    }
    
    /**
     * Encodeur de mots de passe
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuration CORS pour permettre les appels depuis le Gateway
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origines autorisées (à adapter selon l'environnement)
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "https://localhost:*",
            "http://127.0.0.1:*",
            "https://127.0.0.1:*"
        ));

        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"
        ));

        // Headers autorisés
        configuration.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));

        // Headers exposés au client
        configuration.setExposedHeaders(List.of(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Access-Control-Allow-Methods",
            "Access-Control-Max-Age",
            "Access-Control-Allow-Headers",
            "Content-Length",
            "Date",
            "X-Total-Count"
        ));

        // Autoriser les cookies/credentials
        configuration.setAllowCredentials(true);

        // Durée de cache des préflights OPTIONS
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}