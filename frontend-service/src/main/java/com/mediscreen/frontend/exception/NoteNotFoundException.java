package com.mediscreen.frontend.exception;

/**
 * Exception Business : Note non trouvée
 * 
 * Exception découplée des librairies externes (Feign).
 * Utilisée par la couche Repository pour abstraire les FeignExceptions 404.
 * Architecture cohérente avec PatientNotFoundException.
 */
public class NoteNotFoundException extends RuntimeException {
    
    /**
     * Constructeur avec message par défaut
     */
    public NoteNotFoundException() {
        super("Note non trouvée");
    }
    
    /**
     * Constructeur avec message personnalisé
     */
    public NoteNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Factory method pour créer une exception avec ID
     * Utilisé par la couche Repository lors de la transformation des FeignExceptions
     */
    public static NoteNotFoundException withId(String id) {
        return new NoteNotFoundException("Note avec l'ID " + id + " non trouvée");
    }
}