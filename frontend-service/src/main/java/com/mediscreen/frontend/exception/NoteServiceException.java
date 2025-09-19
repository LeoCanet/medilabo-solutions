package com.mediscreen.frontend.exception;

import lombok.Getter;

/**
 * Exception Business : Erreur technique du service Notes
 * 
 * Exception découplée des librairies externes (Feign).
 * Utilisée par la couche Repository pour abstraire les FeignExceptions 500+.
 * Architecture cohérente avec PatientServiceException.
 */
@Getter
public class NoteServiceException extends RuntimeException {
    
    private final int statusCode;
    
    /**
     * Constructeur avec message par défaut
     */
    public NoteServiceException() {
        super("Erreur technique du service Notes");
        this.statusCode = 500;
    }
    
    /**
     * Constructeur avec message personnalisé
     */
    public NoteServiceException(String message) {
        super(message);
        this.statusCode = 500;
    }
    
    /**
     * Constructeur avec message et code de statut
     */
    public NoteServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    /**
     * Constructeur avec cause
     */
    public NoteServiceException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }
}