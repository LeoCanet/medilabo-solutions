package com.mediscreen.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
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
 * Configuration de sécurité Gateway avec routage intelligent et tokens différenciés
 *
 * ARCHITECTURE IMPLEMENTEE :
 * - Frontend s'authentifie avec ses propres credentials vers Gateway
 * - Gateway route intelligemment vers les microservices avec des tokens différenciés :
 *   * Patient Service : credentials spécifiques patient (variables d'environnement)
 *   * Notes Service : credentials spécifiques notes (variables d'environnement)
 *
 * AVANTAGES SÉCURITÉ :
 * - Isolation des credentials par service (pas de partage de tokens)
 * - Gateway agit comme proxy sécurisé avec injection automatique de credentials
 * - Services backend ne connaissent que leurs propres credentials
 * - Credentials stockés uniquement dans variables d'environnement (.env)
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    // Frontend credentials (seul autorisé à contacter le Gateway)
    @Value("${AUTH_USERNAME}")
    private String frontendUsername;

    @Value("${AUTH_PASSWORD}")
    private String frontendPassword;

    // Backend credentials différenciés
    @Value("${mediscreen.auth.patient.username}")
    private String patientUsername;

    @Value("${mediscreen.auth.patient.password}")
    private String patientPassword;

    @Value("${mediscreen.auth.notes.username}")
    private String notesUsername;

    @Value("${mediscreen.auth.notes.password}")
    private String notesPassword;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**", "/debug/**").permitAll()
                        .pathMatchers("/api/**").hasRole("FRONTEND")
                        .anyExchange().authenticated()
                )
                .httpBasic(basic -> {})
                .build();
    }
    
    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        // Frontend user - seul autorisé à contacter le Gateway
        UserDetails frontendUser = User.builder()
                .username(frontendUsername)
                .password(passwordEncoder().encode(frontendPassword))
                .roles("FRONTEND")
                .build();

        return new MapReactiveUserDetailsService(frontendUser);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuration du routage intelligent avec credentials différenciés
     *
     * FONCTIONNEMENT :
     * - Intercepte les requêtes Frontend → Gateway
     * - Route /api/v1/patients/** → Patient Service avec credentials spécifiques patient
     * - Route /api/v1/notes/** → Notes Service avec credentials spécifiques notes
     * - Injection automatique des bons credentials Basic Auth par route
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route Patient Service avec injection automatique credentials patient
                .route("patient-service-route", r -> r
                        .path("/api/v1/patients/**")
                        .filters(f -> f.filter(addPatientAuthHeader()))
                        .uri("http://patient-service:8081")
                )
                // Route Notes Service avec injection automatique credentials notes
                .route("notes-service-route", r -> r
                        .path("/api/v1/notes/**")
                        .filters(f -> f.filter(addNotesAuthHeader()))
                        .uri("http://notes-service:8082")
                )
                .build();
    }

    /**
     * Filtre d'injection automatique des credentials Patient Service
     *
     * PRINCIPE :
     * - Intercepte toutes les requêtes vers /api/v1/patients/**
     * - Remplace le header Authorization du Frontend par les credentials Patient Service
     * - Permet l'isolation des tokens : Frontend ne connaît pas les credentials des microservices
     */
    private GatewayFilter addPatientAuthHeader() {
        return (exchange, chain) -> {
            String credentials = patientUsername + ":" + patientPassword;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

            return chain.filter(exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header(HttpHeaders.AUTHORIZATION, authHeader)
                            .build())
                    .build());
        };
    }

    /**
     * Filtre d'injection automatique des credentials Notes Service
     *
     * PRINCIPE :
     * - Intercepte toutes les requêtes vers /api/v1/notes/**
     * - Remplace le header Authorization du Frontend par les credentials Notes Service
     * - Permet l'isolation des tokens : Frontend ne connaît pas les credentials des microservices
     */
    private GatewayFilter addNotesAuthHeader() {
        return (exchange, chain) -> {
            String credentials = notesUsername + ":" + notesPassword;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

            return chain.filter(exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header(HttpHeaders.AUTHORIZATION, authHeader)
                            .build())
                    .build());
        };
    }
}