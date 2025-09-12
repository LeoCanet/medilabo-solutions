package com.mediscreen.frontend.exception;

import lombok.Getter;

/**
 * Exception Business : Erreur technique du service Patient
 * 
 * Exception découplée des librairies externes (Feign).
 * Utilisée par la couche Repository pour abstraire les FeignExceptions 500+.
 * 
 * Représente les erreurs techniques de communication avec le patient-service,
 * indépendamment de la technologie utilisée (Feign, RestTemplate, WebClient, etc.).
 */
@Getter
public class PatientServiceException extends RuntimeException {
    
    private final int statusCode;
    
    /**
     * Constructeur avec message par défaut
     */
    public PatientServiceException() {
        super("Erreur technique du service Patient");
        this.statusCode = 500;
    }
    
    /**
     * Constructeur avec message personnalisé
     */
    public PatientServiceException(String message) {
        super(message);
        this.statusCode = 500;
    }
    
    /**
     * Constructeur avec message et code de statut
     * Utilisé pour préserver l'information du status HTTP sans exposer Feign
     */
    public PatientServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    /**
     * Constructeur avec cause
     * Utilisé pour encapsuler l'exception originale (FeignException)
     */
    public PatientServiceException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }
    
}