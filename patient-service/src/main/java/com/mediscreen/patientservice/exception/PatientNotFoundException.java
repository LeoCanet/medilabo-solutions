package com.mediscreen.patientservice.exception;

/**
 * Exception levée lorsqu'un patient n'est pas trouvé
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
     * Constructeur avec message et cause
     */
    public PatientNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructeur avec ID du patient
     */
    public PatientNotFoundException(Long patientId) {
        super(STR."Patient avec l'ID \{patientId} non trouvé");
    }
    
    /**
     * Factory method pour créer une exception avec ID
     */
    public static PatientNotFoundException withId(Long id) {
        return new PatientNotFoundException(STR."Patient avec l'ID \{id} non trouvé");
    }
    
    /**
     * Factory method pour créer une exception avec nom complet
     */
    public static PatientNotFoundException withNomComplet(String prenom, String nom) {
        return new PatientNotFoundException(STR."Patient '\{prenom} \{nom}' non trouvé");
    }
}