package com.mediscreen.frontend.exception;

import lombok.Getter;

/**
 * Exception Business : Erreur de validation note
 * 
 * Exception découplée des librairies externes (Feign).
 * Utilisée par la couche Repository pour abstraire les FeignExceptions 400/422.
 * Architecture cohérente avec PatientValidationException.
 * 
 * Encapsule les erreurs de validation du notes-service
 * pour les rendre indépendantes de la technologie de communication.
 */
@Getter
public class NoteValidationException extends RuntimeException {
    
    private final String validationDetails;
    
    /**
     * Constructeur avec message par défaut
     */
    public NoteValidationException() {
        super("Erreur de validation note");
        this.validationDetails = null;
    }
    
    /**
     * Constructeur avec message personnalisé
     */
    public NoteValidationException(String message) {
        super(message);
        this.validationDetails = null;
    }
    
    /**
     * Constructeur avec message et détails de validation
     * Utilisé pour transmettre les erreurs JSON du notes-service
     */
    public NoteValidationException(String message, String validationDetails) {
        super(message);
        this.validationDetails = validationDetails;
    }
}