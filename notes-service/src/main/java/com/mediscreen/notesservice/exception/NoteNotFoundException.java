package com.mediscreen.notesservice.exception;

/**
 * Exception standard : Note non trouvée
 * 
 * Exception simple pour les opérations CRUD MongoDB.
 * Architecture cohérente avec patient-service (exceptions standard uniquement).
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
     * Factory method pour créer une exception avec l'ID de la note
     */
    public static NoteNotFoundException withId(String noteId) {
        return new NoteNotFoundException("Note avec l'ID " + noteId + " non trouvée");
    }
}