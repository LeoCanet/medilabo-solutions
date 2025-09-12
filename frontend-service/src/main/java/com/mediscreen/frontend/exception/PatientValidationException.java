package com.mediscreen.frontend.exception;

import lombok.Getter;

/**
 * Exception Business : Erreur de validation patient
 * 
 * Exception découplée des librairies externes (Feign).
 * Utilisée par la couche Repository pour abstraire les FeignExceptions 400/422.
 * 
 * Encapsule les erreurs de validation du patient-service
 * pour les rendre indépendantes de la technologie de communication.
 */
@Getter
public class PatientValidationException extends RuntimeException {
    
    private final String validationDetails;
    
    /**
     * Constructeur avec message par défaut
     */
    public PatientValidationException() {
        super("Erreur de validation patient");
        this.validationDetails = null;
    }
    
    /**
     * Constructeur avec message personnalisé
     */
    public PatientValidationException(String message) {
        super(message);
        this.validationDetails = null;
    }
    
    /**
     * Constructeur avec message et détails de validation
     * Utilisé pour transmettre les erreurs JSON du patient-service
     */
    public PatientValidationException(String message, String validationDetails) {
        super(message);
        this.validationDetails = validationDetails;
    }
    
}