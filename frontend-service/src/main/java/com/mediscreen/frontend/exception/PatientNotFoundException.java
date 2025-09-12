package com.mediscreen.frontend.exception;

/**
 * Exception Business : Patient non trouvé
 * 
 * Exception découplée des librairies externes (Feign).
 * Utilisée par la couche Repository pour abstraire les FeignExceptions 404.
 *
 */
public class PatientNotFoundException extends RuntimeException {
    
    /**
     * Constructeur avec message par défaut
     */
    public PatientNotFoundException() {
        super("Patient non trouvé");
    }
    
    /**
     * Constructeur avec message personnalisé
     */
    public PatientNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructeur avec ID du patient
     */
    public PatientNotFoundException(Long patientId) {
        super("Patient avec l'ID " + patientId + " non trouvé");
    }
    
    /**
     * Factory method pour créer une exception avec ID
     * Utilisé par la couche Repository lors de la transformation des FeignExceptions
     */
    public static PatientNotFoundException withId(Long id) {
        return new PatientNotFoundException("Patient avec l'ID " + id + " non trouvé");
    }
}