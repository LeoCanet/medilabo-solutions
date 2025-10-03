package com.mediscreen.assessmentservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Base64;

/**
 * Intercepteur Feign pour authentification Basic Auth inter-services
 *
 * Ajoute automatiquement le header "Authorization: Basic <base64>"
 * à toutes les requêtes d'Assessment Service vers le Gateway.
 *
 * Username/password encodés en Base64 et injectés au build Docker.
 */
@Slf4j
@Component
public class BasicAuthFeignInterceptor implements RequestInterceptor {

    @Value("${mediscreen.auth.username}")
    private String username;

    @Value("${mediscreen.auth.password}")
    private String password;

    @Override
    public void apply(RequestTemplate template) {

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            log.warn("Credentials Basic Auth non configurés - Communications non sécurisées");
            return;
        }

        // Création du header Basic Auth
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        template.header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);

        log.debug("Header Basic Auth ajouté pour l'appel : {} {}",
                 template.method(), template.url());
    }
}