package com.mediscreen.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.Base64;

/**
 * Configuration de sécurité Gateway avec Basic Auth
 * 
 * Configure l'authentification Basic Auth pour valider les requêtes
 * des services frontend vers les services backend.
 * 
 * Username/password décodés depuis les variables Base64 Docker.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    
    @Value("${mediscreen.auth.username}")
    private String encodedUsername;
    
    @Value("${mediscreen.auth.password}")
    private String encodedPassword;
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .anyExchange().authenticated()
                )
                .httpBasic(basic -> {})
                .build();
    }
    
    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        // Décodage des credentials depuis Base64
        String username = new String(Base64.getDecoder().decode(encodedUsername));
        String password = new String(Base64.getDecoder().decode(encodedPassword));
        
        UserDetails user = User.builder()
                .username(username)
                .password(passwordEncoder().encode(password))
                .roles("SERVICE")
                .build();
        
        return new MapReactiveUserDetailsService(user);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}